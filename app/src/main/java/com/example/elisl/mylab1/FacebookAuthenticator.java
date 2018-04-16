package com.example.elisl.mylab1;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * Facebook Authenticator
 *
 * This class handles login and sign up through Facebook
 * Author: Elis Lulja
 * Last Revision: April 16 2018, 16:39
 * To-DOs written in the source.
 */
public class FacebookAuthenticator
{
    /**
     * The action types
     */
    public enum ActionTypes {UNKNOWN, SIGNUP, LOGIN};

    /**
     * The button
     */
    private LoginButton Button = null;

    /**
     * The callback manager.
     */
    private CallbackManager Manager = null;

    /**
     * The current context
     * @param context
     */
    private Context context = null;

    /**
     * What are we doing?
     */
    private ActionTypes Type = ActionTypes.UNKNOWN;

    /**
     * The activity I have been called from
     */
    private Activity CurrentActivity = null;

    /**
     * The constructor
     * @param context
     */
    FacebookAuthenticator(Context context, Activity currentActivity)
    {
        this.context = context;
        Manager = CallbackManager.Factory.create();
        CurrentActivity = currentActivity;
    }

    /**
     * Set the button
     * @param button the button
     */
    public void setButton(LoginButton button)
    {
        Button = button;

        //  Set the callback
        Button.registerCallback(Manager, new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult)
                    {
                        Log.d("FBLOGIN", "Successful!");
                        onFinish(loginResult.getAccessToken());
                    }

                    @Override
                    public void onCancel()
                    {
                        Log.d("FBLOGIN", "Operation cancelled by user");
                    }

                    @Override
                    public void onError(FacebookException error)
                    {
                        Log.d("FBLOGIN", "An error occurred while getting user data");
                        Toast.makeText(context, "Error", Toast.LENGTH_LONG).show();
                    }
                }
        );
    }

    /**
     * Set the action type (what to do when button is clicked
     * @param type the type. Permitted: login, signup
     */
    public void setActionType(ActionTypes type)
    {
        Type = type;
    }

    /**
     * This gets called when the login/signup procedure is concluded
     * @param token the access token from facebook
     */
    protected void onFinish(AccessToken token)
    {
        Log.d("FBLOGIN", "onFinish called!");

        if(Type == ActionTypes.UNKNOWN) return;

        //---------------------------------
        //  Sign up
        //---------------------------------

        if(Type == ActionTypes.SIGNUP)
        {
            getMe(token);
            //CurrentActivity.finish();
        }
    }

    /**
     * This gets the activity result from the HTTP request to this class.
     * @param requestCode the code of the request (from android os)
     * @param resultCode the code of the response (HTTP)
     * @param data the intent with the data
     */
    public void setActivityResult(int requestCode, int resultCode, Intent data)
    {
        Manager.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Get connected user data after a successful signup
     * @param token the token from facebook
     */
    private void getMe(AccessToken token)
    {
        GraphRequest me = GraphRequest.newMeRequest(token, new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject object, GraphResponse response)
            {
                Log.d("FBLOGIN", "newMeRequest is finished with status: " + response.getRawResponse());

                //  200 OK?
                if(response.getError() != null)
                {
                    Toast.makeText(context, context.getResources().getText(R.string.fb_error_get_me), Toast.LENGTH_SHORT).show();
                    return;
                }

                JSONObject data = response.getJSONObject();
                try
                {
                    Log.d("FBLOGIN", "name: " + data.getString("name"));
                    Log.d("FBLOGIN", "first_name: " + data.getString("first_name"));
                    Log.d("FBLOGIN", "last_name: " + data.getString("last_name"));
                    Log.d("FBLOGIN", "gender: " + data.getString("gender"));
                    Log.d("FBLOGIN", "locale: " + data.getString("locale"));
                    Log.d("FBLOGIN", "timezone: " + data.getString("timezone"));
                    Log.d("FBLOGIN", "verified: " + data.getString("verified"));
                    Log.d("FBLOGIN", "email: " + data.getString("email"));
                }
                catch(JSONException j)
                {
                    Log.e("FBLOGIN", "Excption occurred");
                }
            }
        });

        //  Query parameters for the request
        Bundle params = new Bundle();
        params.putString("fields", context.getResources().getString(R.string.fields));
        me.setParameters(params);
        me.executeAsync();
    }
}
