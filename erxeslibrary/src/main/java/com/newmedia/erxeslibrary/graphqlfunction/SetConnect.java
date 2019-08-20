package com.newmedia.erxeslibrary.graphqlfunction;

import android.content.Context;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.apollographql.apollo.rx2.Rx2Apollo;
import com.newmedia.erxes.basic.MessengerConnectMutation;
import com.newmedia.erxeslibrary.configuration.Config;
import com.newmedia.erxeslibrary.configuration.ErxesRequest;
import com.newmedia.erxeslibrary.configuration.ErxesHelper;
import com.newmedia.erxeslibrary.configuration.Returntype;
import com.newmedia.erxeslibrary.DataManager;
import com.newmedia.erxeslibrary.helper.Json;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;


public class SetConnect {
    final static String TAG = "SETCONNECT";
    private ErxesRequest erxesRequest;
    private Config config;
    private DataManager dataManager;

    public SetConnect(ErxesRequest erxesRequest, Context context) {
        this.erxesRequest = erxesRequest;
        config = Config.getInstance(context);
        dataManager = DataManager.getInstance(context);
    }

    public void run(String email, String phone, boolean isUser, String data) {
        JSONObject customData = new JSONObject();
        try {
            if (data != null) {
                customData = new JSONObject(data);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        MessengerConnectMutation mutate = MessengerConnectMutation.builder()
                .brandCode(config.brandCode)
                .email(email)
                .phone(phone)
                .isUser(isUser)
                .data(new Json(customData))
                .build();
        Rx2Apollo.from(erxesRequest.apolloClient
                .mutate(mutate))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }
    private Observer observer = new Observer<Response<MessengerConnectMutation.Data>>() {
        @Override
        public void onSubscribe(Disposable d) {

        }

        @Override
        public void onNext(Response<MessengerConnectMutation.Data> response) {
            if (!response.hasErrors()) {
                config.customerId = response.data().messengerConnect().customerId();
                config.integrationId = response.data().messengerConnect().integrationId();

                dataManager.setData(DataManager.CUSTOMERID, config.customerId);
                dataManager.setData(DataManager.INTEGRATIONID, config.integrationId);

                config.changeLanguage(response.data().messengerConnect().languageCode());
                ErxesHelper.load_uiOptions(response.data().messengerConnect().uiOptions());
                ErxesHelper.load_messengerData(response.data().messengerConnect().messengerData());

                erxesRequest.notefyAll(Returntype.LOGINSUCCESS, null, null);
            } else {
                erxesRequest.notefyAll(Returntype.SERVERERROR, null, response.errors().get(0).message());
            }
        }

        @Override
        public void onError(Throwable e) {
            e.printStackTrace();
            erxesRequest.notefyAll(Returntype.CONNECTIONFAILED,null,e.getMessage());

        }

        @Override
        public void onComplete() {

        }
    };
}
