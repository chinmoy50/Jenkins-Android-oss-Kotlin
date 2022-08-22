package com.kickstarter.viewmodels

import androidx.annotation.NonNull
import com.kickstarter.R
import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.Environment
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.extensions.MINIMUM_PASSWORD_LENGTH
import com.kickstarter.ui.activities.SetPasswordActivity
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

interface SetPasswordViewModel {

    interface Inputs {
        /** Call when the user clicks the change password button. */
        fun changePasswordClicked()

        /** Call when the current password field changes.  */
        fun confirmPassword(confirmPassword: String)

        /** Call when the new password field changes.  */
        fun newPassword(newPassword: String)
    }

    interface Outputs {
        /** Emits when the password update was unsuccessful. */
        fun error(): Observable<String>

        /** Emits a string resource to display when the user's new password entries are invalid. */
        fun passwordWarning(): Observable<Int>

        /** Emits when the progress bar should be visible. */
        fun progressBarIsVisible(): Observable<Boolean>

        /** Emits when the save button should be enabled. */
        fun saveButtonIsEnabled(): Observable<Boolean>

        /** Emits when the password update was successful. */
        fun success(): Observable<String>

        /** Emits a boolean that determines if the form is in the progress of being submitted. */
        fun isFormSubmitting(): Observable<Boolean>
    }

    class ViewModel(@NonNull val environment: Environment) : ActivityViewModel<SetPasswordActivity>(environment), Inputs, Outputs {

        private val changePasswordClicked = PublishSubject.create<Void>()
        private val confirmPassword = PublishSubject.create<String>()
        private val newPassword = PublishSubject.create<String>()

        private val error = BehaviorSubject.create<String>()
        private val passwordWarning = BehaviorSubject.create<Int>()
        private val progressBarIsVisible = BehaviorSubject.create<Boolean>()
        private val saveButtonIsEnabled = BehaviorSubject.create<Boolean>()
        private val success = BehaviorSubject.create<String>()
        private val isFormSubmitting = PublishSubject.create<Boolean>()

        val inputs: Inputs = this
        val outputs: Outputs = this

        private val apolloClient = requireNotNull(this.environment.apolloClient())
        private val analytics = this.environment.analytics()

        init {

            val changePassword = Observable.combineLatest(
                this.newPassword.startWith(""),
                this.confirmPassword.startWith(""),
                { new, confirm -> ChangePassword(new, confirm) }
            )

            changePassword
                .map { it.warning() }
                .distinctUntilChanged()
                .compose(bindToLifecycle())
                .subscribe(this.passwordWarning)

            changePassword
                .map { it.isValid() }
                .distinctUntilChanged()
                .compose(bindToLifecycle())
                .subscribe(this.saveButtonIsEnabled)

            val changePasswordNotification = changePassword
                .compose(Transformers.takeWhen(this.changePasswordClicked))
                .switchMap { cp -> submit(cp).materialize() }
                .compose(bindToLifecycle())
                .share()

            changePasswordNotification
                .compose(Transformers.errors())
                .subscribe({ this.error.onNext(it.localizedMessage) })

            changePasswordNotification
                .compose(Transformers.values())
                .map { it.updateUserAccount()?.user()?.email() }
                .subscribe {
                    this.analytics?.reset()
                    this.success.onNext(it)
                }
        }

        private fun submit(changePassword: ChangePassword): Observable<UpdateUserPasswordMutation.Data> {
            return this.apolloClient.setUserPassword(changePassword.newPassword, changePassword.confirmPassword)
                .doOnSubscribe {
                    this.progressBarIsVisible.onNext(true)
                    this.isFormSubmitting.onNext(true)
                }
                .doAfterTerminate {
                    this.progressBarIsVisible.onNext(false)
                    this.isFormSubmitting.onNext(false)
                }
        }

        override fun changePasswordClicked() {
            this.changePasswordClicked.onNext(null)
        }

        override fun confirmPassword(confirmPassword: String) {
            this.confirmPassword.onNext(confirmPassword)
        }

        override fun newPassword(newPassword: String) {
            this.newPassword.onNext(newPassword)
        }

        override fun error(): Observable<String> {
            return this.error
        }

        override fun passwordWarning(): Observable<Int> {
            return this.passwordWarning
        }

        override fun progressBarIsVisible(): Observable<Boolean> {
            return this.progressBarIsVisible
        }

        override fun saveButtonIsEnabled(): Observable<Boolean> {
            return this.saveButtonIsEnabled
        }

        override fun isFormSubmitting(): Observable<Boolean> {
            return this.isFormSubmitting
        }

        override fun success(): Observable<String> {
            return this.success
        }

        data class ChangePassword(val newPassword: String, val confirmPassword: String) {
            fun isValid(): Boolean {
                return isNotEmptyAndAtLeast6Chars(this.newPassword) &&
                    isNotEmptyAndAtLeast6Chars(this.confirmPassword) &&
                    this.confirmPassword == this.newPassword
            }

            fun warning(): Int? {
                return if (newPassword.isNotEmpty() && newPassword.length in 1 until MINIMUM_PASSWORD_LENGTH)
                    R.string.Password_min_length_message
                else if (confirmPassword.isNotEmpty() && confirmPassword != newPassword)
                    R.string.Passwords_matching_message
                else null
            }

            private fun isNotEmptyAndAtLeast6Chars(password: String) = !password.isEmpty() && password.length >= MINIMUM_PASSWORD_LENGTH
        }
    }
}