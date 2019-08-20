package com.newmedia.erxeslibrary.graphqlfunction;

import android.content.Context;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.api.cache.http.HttpCachePolicy;
import com.apollographql.apollo.exception.ApolloException;
import com.apollographql.apollo.rx2.Rx2Apollo;
import com.newmedia.erxes.basic.MessengerSupportersQuery;
import com.newmedia.erxeslibrary.configuration.Config;
import com.newmedia.erxeslibrary.configuration.ErxesRequest;
import com.newmedia.erxeslibrary.configuration.Returntype;
import com.newmedia.erxeslibrary.model.User;

import org.jetbrains.annotations.NotNull;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class GetSupporter {
    final static String TAG = "GETSUP";
    private ErxesRequest erxesRequest;
    private Config config;

    public GetSupporter(ErxesRequest erxesRequest, Context context) {
        this.erxesRequest = erxesRequest;
        config = Config.getInstance(context);
    }

    public void run() {
        MessengerSupportersQuery query = MessengerSupportersQuery.builder()
                .integ(config.integrationId).build();
        Rx2Apollo.from(erxesRequest.apolloClient
                .query(query)
                .httpCachePolicy(HttpCachePolicy.CACHE_FIRST))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }
    private Observer observer = new Observer<Response<MessengerSupportersQuery.Data>>() {
        @Override
        public void onSubscribe(Disposable d) {

        }

        @Override
        public void onNext(Response<MessengerSupportersQuery.Data> response) {
            if (!response.hasErrors()) {
                if (config.supporters != null && config.supporters.size() > 0)
                    config.supporters.clear();
                if (config.supporters != null) {
                    config.supporters.addAll(User.convert(response.data().messengerSupporters()));
                }
                erxesRequest.notefyAll(Returntype.GETSUPPORTERS, null, null);
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
