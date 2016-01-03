package au.com.tilbrook.android.rxkotlin.fragments

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.text.InputType.*
import android.text.TextUtils.isEmpty
import android.util.Patterns.EMAIL_ADDRESS
import android.view.Gravity.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout.HORIZONTAL
import android.widget.TextView
import au.com.tilbrook.android.rxkotlin.R
import au.com.tilbrook.android.rxkotlin.utils.unSubscribeIfNotNull
import com.jakewharton.rxbinding.widget.RxTextView
import org.jetbrains.anko.*
import org.jetbrains.anko.support.v4.ctx
import rx.Observable
import rx.Observer
import rx.Subscription
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

        _emailChangeObservable = RxTextView.textChanges(_email).skip(1)
        _passwordChangeObservable = RxTextView.textChanges(_password).skip(1)
        _numberChangeObservable = RxTextView.textChanges(_number).skip(1)

        _combineLatestEvents()

        return layout
    }

    override fun onPause() {
        super.onPause()
        _subscription.unSubscribeIfNotNull()
    }

    private fun _combineLatestEvents() {
        _subscription = Observable.combineLatest(
            _emailChangeObservable,
            _passwordChangeObservable,
            _numberChangeObservable
        ) { newEmail, newPassword, newNumber ->
            val emailValid = !isEmpty(newEmail) && EMAIL_ADDRESS.matcher(newEmail).matches()
            if (!emailValid) {
                _email.error = "Invalid Email!"
            }

            val passValid = !isEmpty(newPassword) && newPassword.length > 8
            if (!passValid) {
                _password.error = "Invalid Password!"
            }

            var numValid = !isEmpty(newNumber)
            if (numValid) {
                val num = Integer.parseInt(newNumber.toString())
                numValid = num > 0 && num <= 100
            }
            if (!numValid) {
                _number.error = "Invalid Number!"
            }

            emailValid && passValid && numValid
        }.subscribe(
            { formValid ->
                val color = if(formValid) R.color.blue else R.color.gray
                _btnValidIndicator.setBackgroundColor(ContextCompat.getColor(ctx, color))
            },
            { Timber.e(it, "there was an error") },
            { Timber.d("completed")}
        )
    }
}
