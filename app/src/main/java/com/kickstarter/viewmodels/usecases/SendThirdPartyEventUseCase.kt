package com.kickstarter.viewmodels.usecases

import android.content.SharedPreferences
import android.util.Pair
import com.kickstarter.libs.CurrentUserType
import com.kickstarter.libs.CurrentUserTypeV2
import com.kickstarter.libs.featureflag.FeatureFlagClientType
import com.kickstarter.libs.featureflag.FlagKey
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.ThirdPartyEventValues
import com.kickstarter.models.Project
import com.kickstarter.services.ApolloClientType
import com.kickstarter.services.ApolloClientTypeV2
import com.kickstarter.ui.SharedPreferenceKey
import com.kickstarter.ui.data.CheckoutData
import com.kickstarter.ui.data.PledgeData
import rx.Observable

class SendThirdPartyEventUseCase(
    sharedPreferences: SharedPreferences,
    ffClient: FeatureFlagClientType,
) : BuildInput {
    private val canSendEventFlag = (
        ffClient.getBoolean(FlagKey.ANDROID_CONSENT_MANAGEMENT) &&
            sharedPreferences.getBoolean(SharedPreferenceKey.CONSENT_MANAGEMENT_PREFERENCE, false) &&
            (ffClient.getBoolean(FlagKey.ANDROID_CAPI_INTEGRATION) || ffClient.getBoolean(FlagKey.ANDROID_GOOGLE_ANALYTICS))
        )

    /**
     * Send third party analytics events, with it's properties.
     *
     * @param draftPledge pledge holds the values for pledgeAmount and Shipping amount required for the analytics events, when the Checkout has not taken place yet.
     * and example for this type of events will be ThirdPartyEventValues.EventName.ADD_PAYMENT_INFO, ad the payment methods can change or be added before the
     * pledge is done.
     *
     * @param checkoutAndPledgeData.first holds the information around Checkout, this information is only available once the user hits pledge and becomes a backer.
     * @param checkoutAndPledgeData.second holds the user selection of reward/addOns, selected location.
     */
    fun sendThirdPartyEvent(
        project: Observable<Project>,
        apolloClient: ApolloClientType,
        checkoutAndPledgeData: Observable<Pair<CheckoutData, PledgeData>?> = Observable.just(Pair(null, null)),
        currentUser: CurrentUserType,
        eventName: ThirdPartyEventValues.EventName,
        firebaseScreen: String = "",
        firebasePreviousScreen: String = "",
        draftPledge: Pair<Double, Double>? = null,
    ): Observable<Pair<Boolean, String>> {

        return project
            .filter { it.sendThirdPartyEvents() ?: false && canSendEventFlag }
            .compose(Transformers.combineLatestPair(currentUser.observable()))
            .compose(Transformers.combineLatestPair(checkoutAndPledgeData))
            .map {
                this.buildInput(
                    eventName = eventName,
                    canSendEventFlag = canSendEventFlag,
                    firebaseScreen = firebaseScreen,
                    firebasePreviousScreen = firebasePreviousScreen,
                    draftPledge = draftPledge,
                    rawData = it
                )
            }
            .switchMap { input ->
                apolloClient.triggerThirdPartyEvent(
                    input
                )
                    .compose(Transformers.neverError()).share()
            }
            .share()
    }

    /**
     * Send third party analytics events, with it's properties.
     *
     * @param draftPledge pledge holds the values for pledgeAmount and Shipping amount required for the analytics events, when the Checkout has not taken place yet.
     * and example for this type of events will be ThirdPartyEventValues.EventName.ADD_PAYMENT_INFO, ad the payment methods can change or be added before the
     * pledge is done.
     *
     * @param checkoutAndPledgeData.first holds the information around Checkout, this information is only available once the user hits pledge and becomes a backer.
     * @param checkoutAndPledgeData.second holds the user selection of reward/addOns, selected location.
     */
    fun sendThirdPartyEventV2(
            project: io.reactivex.Observable<Project>,
            apolloClient: ApolloClientTypeV2,
            checkoutAndPledgeData: io.reactivex.Observable<Pair<CheckoutData, PledgeData>?> = io.reactivex.Observable.just(Pair(null, null)),
            currentUser: CurrentUserTypeV2,
            eventName: ThirdPartyEventValues.EventName,
            firebaseScreen: String = "",
            firebasePreviousScreen: String = "",
            draftPledge: Pair<Double, Double>? = null,
    ): io.reactivex.Observable<Pair<Boolean, String>> {

        return project
                .filter { it.sendThirdPartyEvents() ?: false && canSendEventFlag }
                .compose(Transformers.combineLatestPair(currentUser.observable()))
                .compose(Transformers.combineLatestPair(checkoutAndPledgeData))
                .map {
                    this.buildInput(
                            eventName = eventName,
                            canSendEventFlag = canSendEventFlag,
                            firebaseScreen = firebaseScreen,
                            firebasePreviousScreen = firebasePreviousScreen,
                            draftPledge = draftPledge,
                            rawData = Pair.create(Pair.create(it.first.first, it.first.second.getValue()), it.second)
                    )
                }
                .switchMap { input ->
                    apolloClient.triggerThirdPartyEvent(
                            input
                    )
                            .compose(Transformers.neverErrorV2()).share()
                }
                .share()
    }
}
