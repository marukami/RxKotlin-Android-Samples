package au.com.tilbrook.android.rxkotlin.fragments

import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.text.InputType.*
import android.text.TextUtils.isEmpty
import android.util.Patterns.EMAIL_ADDRESS
import android.view.Gravity.CENTER
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout.HORIZONTAL
import android.widget.TextView
import au.com.tilbrook.android.rxkotlin.R
import au.com.tilbrook.android.rxkotlin.utils.backgroundColorByResId
import au.com.tilbrook.android.rxkotlin.utils.unSubscribeIfNotNull
import com.jakewharton.rxbinding.widget.RxTextView
import org.jetbrains.anko.*
import org.jetbrains.anko.support.v4.ctx
import rx.Observable
import rx.Subscription
import rx.functions.Func1
import timber.log.Timber

class FormValidationCombineLatestFragment : BaseFragment() {

    private lateinit var _btnValidIndicator: TextView
    private lateinit var _email: EditText
    private lateinit var _password: EditText
    private lateinit var _number: EditText

    private lateinit var _emailChangeObservable: Observable<CharSequence>
    private lateinit var _passwordChangeObservable: Observable<CharSequence>
    private lateinit var _numberChangeObservable: Observable<CharSequence>

    private var _subscription: Subscription? = null

    override fun onCreateView(inflater: LayoutInflater?,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val layout = with(ctx) {
            verticalLayout {
                lparams(width = matchParent, height = matchParent)
                textView(R.string.msg_demo_form_comb_latest) {
                    lparams (width = matchParent)
                    padding = dip(10)
                    this.gravity = CENTER
                }
                linearLayout {
                    orientation = HORIZONTAL
                    lparams(width = matchParent)
                    textView("Enter a valid email below:") {
                        lparams(width = 0, weight = 1f)
                        padding = dip(10)
                    }
                    _email = editText {
                        lparams(width = 0, weight = 1f)
                        inputType = TYPE_CLASS_TEXT or TYPE_TEXT_VARIATION_EMAIL_ADDRESS
                    }
                }
                linearLayout {
                    orientation = HORIZONTAL
                    lparams(width = matchParent)
                    textView("password (> than 8 chrs):") {
                        lparams(width = 0, weight = 1f)
                        padding = dip(10)
                    }
                    _password = editText {
                        lparams(width = 0, weight = 1f)
                    }
                }
                linearLayout {
                    orientation = HORIZONTAL
                    lparams(width = matchParent)
                    textView("number (between 1 & 100):") {
                        lparams(width = 0, weight = 1f)
                        padding = dip(10)
                    }
                    _number = editText {
                        lparams(width = 0, weight = 1f)
                        inputType = TYPE_CLASS_NUMBER or TYPE_NUMBER_VARIATION_NORMAL
                    }
                }
                _btnValidIndicator = textView("Submit") {
                    lparams {
                        topMargin = dip(10)
                        gravity = CENTER
                    }
                    padding = dip(10)
                    backgroundColor = ContextCompat.getColor(ctx, R.color.gray)
                }
            }
        }

        _emailChangeObservable = RxTextView.textChanges(_email)
        _passwordChangeObservable = RxTextView.textChanges(_password)
        _numberChangeObservable = RxTextView.textChanges(_number)

        return layout
    }

    override fun onStart() {
        super.onStart()
        _combineLatestEvents()
    }

    override fun onStop() {
        super.onStop()
        _subscription.unSubscribeIfNotNull()
    }

    private fun _combineLatestEvents() {
        _subscription = Observable.combineLatest(
            _emailChangeObservable,
            _passwordChangeObservable,
            _numberChangeObservable
        ) { newEmail, newPassword, newNumber -> Form(newEmail, newPassword, newNumber) }
            .onBackpressureDrop()
            .skipWhile(FormIsClean())
            .map { form ->
                form.whenNotTrue(form.isEmailValid) {
                    _email.error = "Invalid Email!"
                }
                form.whenNotTrue(form.isPasswordValid) {
                    _password.error = "Invalid Password!"
                }
                form.whenNotTrue(form.isNumberValid) {
                    _number.error = "Invalid Number!"
                }

                form.valid
            }.subscribe(
            { isValid ->
                _btnValidIndicator.backgroundColorByResId =
                    if (isValid) R.color.blue
                    else R.color.gray
            },
            { Timber.e(it, "there was an error") },
            { Timber.d("completed") })
    }

    private class FormIsClean : Func1<Form, Boolean> {

        private var isDirty = false

        override fun call(formValidator: Form): Boolean? {
            if (!isDirty) {
                isDirty = formValidator.allFieldsDirty
            }
            return !isDirty
        }
    }

    class Form(
        private val newEmail: CharSequence,
        private val newPassword: CharSequence,
        private val newNumber: CharSequence
    ) {
        val isEmailValid: Boolean
        val isNumberValid: Boolean
        val isPasswordValid: Boolean
        val valid: Boolean
        val allFieldsDirty: Boolean

        init {
            isEmailValid = isEmpty(newEmail).not() && EMAIL_ADDRESS.matcher(newEmail).matches()
            isPasswordValid = isEmpty(newPassword).not() && newPassword.length > 8
            isNumberValid = isEmpty(newNumber).not() && isBetween(
                start = 0,
                mid = {
                    newNumber.toString().
                        toNumberOrDefault(String::toInt, 0) as Int
                },
                end = 100
            )
            valid = isEmailValid and isNumberValid and isPasswordValid
            allFieldsDirty = !isEmpty(newEmail) and !isEmpty(newPassword) and !isEmpty(newNumber)
        }

        inline fun isBetween(mid: () -> Int, start: Int, end: Int): Boolean {
            return (mid() > start) and (mid() <= end)
        }
    }

    inline fun Form.whenNotTrue(bool: Boolean, whenInvalid: Form.() -> Unit) {
        if (bool.not()) {
            whenInvalid()
        }
    }
}

inline fun String.toNumberOrDefault(cast: String.() -> Number, default: Number):
    Number {
    try {
        return cast()
    } catch (ex: NumberFormatException) {
        return default;
    }
}
