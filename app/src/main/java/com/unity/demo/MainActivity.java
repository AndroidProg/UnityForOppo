package com.unity.demo;

import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;
import com.nearme.game.sdk.GameCenterSDK;
import com.nearme.game.sdk.callback.ApiCallback;
import com.nearme.game.sdk.callback.GameExitCallback;
import com.nearme.game.sdk.common.model.biz.PayInfo;
import com.nearme.game.sdk.common.model.biz.ReportUserGameInfoParam;
import com.unity3d.player.UnityPlayer;
import com.unity3d.player.UnityPlayerActivity;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class MainActivity extends UnityPlayerActivity {
    private static String appSecret = "149210d64b5e0b8eE0B6e5DAf642229F";
    public final  String TAG="UnityForOppo";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        doInit();
    }
    /**
     * 初始化
     */
    public void doInit(){
        Log.i(TAG,"DoInit");
        GameCenterSDK.init(appSecret, this);

        Toast.makeText(MainActivity.this,"初始化成功",Toast.LENGTH_LONG).show();
    }

    /**
     * 登录
     */
    public void doLogin(){
        Log.i(TAG,"DoLogin");
        GameCenterSDK.getInstance().doLogin(this, new ApiCallback() {
            //登录成功的回调
            @Override
            public void onSuccess(String msg) {
                Toast.makeText(MainActivity.this,"获取登录状态:====="+msg.toString(),Toast.LENGTH_LONG).show();
                GameCenterSDK.getInstance().doGetTokenAndSsoid(new ApiCallback() {
                    @Override
                    public void onSuccess(String resultMsg) {
                        Log.i(TAG,"登录成功获取的msg：====="+resultMsg.toString());
                        String jsonString = "";
                        try {
                            JSONObject   json = new JSONObject(resultMsg);
                            String token = json.getString("token");
                            String ssoid = json.getString("ssoid");
                            json.put("token", URLEncoder.encode(token, "UTF-8"));
                            json.put("ssoid",json.getString("ssoid"));
                            if(json==null){
                                return;
                            }
                            jsonString=json.toString();
                            Log.i(TAG,"登录成功向Unity发送消息：====="+jsonString);
                            /**
                             * 向Unity传递消息
                             * 第1个参数为Unity场景中用于接收android消息的对象名称
                             * 第2个参数为对象上的脚本的一个成员方法名称（脚本名称不限制）
                             * 第3个参数为Unity方法的参数
                             */
                            UnityPlayer.UnitySendMessage("AndroidSDKListener", "LoginCallback", jsonString);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(String resultMsg, int code) {
                        UnityPlayer.UnitySendMessage("AndroidSDKListener", "LoginCallback", resultMsg);
                    }
                });

            }
            //登录失败的回调
            @Override
            public void onFailure(String msg, int code) {
                //回调oppo登录失败，把失败的消息发给Unity
                UnityPlayer.UnitySendMessage("AndroidSDKListener", "LoginCallback", msg);
            }
        });
    }

    /**
     * 支付(唤起支付参数为模拟数据 真实应该由游戏服务器端传入)
     */
    public void doPay(){
        Log.i(TAG,"DoPay");
        String y2_order=System.currentTimeMillis()+"";//订单号 建议由游戏服务器提供
        String productDesc="优惠月礼包"; //商品描述
        String  productName="钻石"; //商品名称
        String  GAME_OPPO_URL="http://192.168.1.1:8080/order/oppo";//支付回调地址，有服务器提供
        //参数1  游戏订单号  参数2 附加参数 传什么都可以 这里传入订单号  参数3 为支付金额 单位分
        PayInfo payInfo = new PayInfo(y2_order, y2_order, 1);
        payInfo.setProductDesc(productDesc);
        payInfo.setProductName(productName);
        payInfo.setCallbackUrl(GAME_OPPO_URL);
        GameCenterSDK.getInstance().doPay(this, payInfo, new ApiCallback() {

            @Override
            public void onSuccess(String msg) {
                Log.i(TAG,"PAY SUC");
            }

            @Override
            public void onFailure(String msg, int code) {
                Log.i(TAG,"PAY Fail========"+msg);
            }
        });
    }

    /**
     * 上报游戏数据 给OPPO
     */
    public  void doSubUserInfo (){
        Map<String, String> mRoleInfo =new HashMap<String, String>();
        mRoleInfo.put("type", "createRole");// 以下场景必传[enterServer（登录），levelUp（升级），createRole（创建角色），exitServer（退出）]
        mRoleInfo.put("roleId", "123456");// 当前登录的玩家角色ID,若无，可传入userid
        mRoleInfo.put("roleName", "天下第一");// 当前登录的玩家角色名,不能空
        mRoleInfo.put("roleLevel", "10");// 当前登录的玩家角色等级，不能为空，必须为数字，且不能为null，若无，传入0
        mRoleInfo.put("serverId", "1001");// 当前登录的游戏区服ID，不能为空，必须为数字，若无，传入0
        mRoleInfo.put("serverName", "Oppo32服");// 当前登录的游戏区服名称,不能为空,长度不超过50，不能为null，若无，传入“无”

        if (mRoleInfo != null && "createRole".equals(mRoleInfo.get("type")) || ("enterServer".equals(mRoleInfo.get("type"))) || ("levelUp".equals(mRoleInfo.get("type")))) {
            String serverId = mRoleInfo.get("serverId");
            String serverName = mRoleInfo.get("serverName");
            String roleId = mRoleInfo.get("roleId");
            String roleName = mRoleInfo.get("roleName");
            String roleLevel = mRoleInfo.get("roleLevel");
            GameCenterSDK.getInstance().doReportUserGameInfoData(new ReportUserGameInfoParam(roleId, roleName, Integer.valueOf(roleLevel), roleId, roleName, "", new TreeMap<String, Number>()), new ApiCallback() {
                @Override
                public void onSuccess(String msg) {
                }
                @Override
                public void onFailure(String msg, int code) {

                }
            });
        }

    }
    /**
     * 退出游戏
     */
    public void  doExitGame(){
        Log.i(TAG,"DoExitGame");
        GameCenterSDK.getInstance().onExit(this,new GameExitCallback() {
            @Override
            public void exitGame() {
                finish();
                System.exit(0);
                android.os.Process.killProcess(android.os.Process.myPid());
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.i(TAG, "onKeyDown");
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            Log.i(TAG, "DoExitGameDoExitGameDoExitGame");
            doExitGame();
            return false;
        } else {
            Log.i(TAG, "111");
            return super.onKeyDown(keyCode, event);
        }
    }

}
