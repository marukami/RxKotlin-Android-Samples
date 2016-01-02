package au.com.tilbrook.android.rxkotlin.fragments

import android.os.Bundle
import android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
import android.text.TextUtils.isEmpty
import android.view.Gravity.CENTER
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout.HORIZONTAL
import android.widget.TextView
import au.com.tilbrook.android.rxkotlin.R
import au.com.tilbrook.android.rxkotlin.utils.unSubscribeIfNotNull
import org.jetbrains.anko.*
import org.jetbrains.anko.support.v4.ctx
import rx.Subscription
import rx.subjects.PublishSubject

class DoubleBindingTextViewFragment : BaseFragment() {

    private lateinit var _number1: EditText
    private lateinit var _number2: EditText
    private lateinit var _result: TextView
    private lateinit var _subscription: Subscription
    private lateinit var _resultEmitterSubject: PublishSubject<Float>

    override fun onCreateView(inflater: LayoutInflater?,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val layout = with(ctx) {
            verticalLayout {
                textView(R.string.msg_demo_doublebinding){
                    lparams(width = matchParent)
                    padding = dip(10)
                }.gravity = CENTER
                linearLayout {
                    lparams(width = matchParent) {
                        topMargin = dip(10)
                    }

                    val listener = __TextWatcher()
                    listener.afterTextChanged { onNumberChanged() }

                    orientation = HORIZONTAL
                    _number1 = editText("100") {
                        lparams(width = 0, height = dip(50), weight = 1f)
                        inputType = TYPE_NUMBER_FLAG_DECIMAL
                        addTextChangedListener(listener)
                    }
                    _number1.gravity = CENTER
                    textView("+") {
                        lparams(width = 0, height = dip(50), weight = 1f)
                    }.gravity = CENTER
                    _number2 = editText("8") {
                        lparams(width = 0, height = dip(50), weight = 1f)
                        inputType = TYPE_NUMBER_FLAG_DECIMAL
                        addTextChangedListener(listener)
                    }
                    _number2.gravity = CENTER
                }
                _result = textView("0") {
                    lparams(width = matchParent) {
                        topMargin = dip(10)
                    }
                    textSize = 45f
                }
                _result.gravity = CENTER
            }
        }

        _resultEmitterSubject = PublishSubject.create<Float>()
        _subscription = _resultEmitterSubject
            .asObservable()
            .subscribe { aFloat -> _result.text = aFloat.toString() }

        onNumberChanged()
        _number2.requestFocus()

        return layout
    }

    //    @OnTextChanged(R.id.double_binding_num1, R.id.double_binding_num2)
    fun onNumberChanged() {
        var num1 = 0f
        var num2 = 0f

        if (!isEmpty(_number1.text.toString())) {
            num1 = java.lang.Float.parseFloat(_number1.text.toString())
        }

        if (!isEmpty(_number2.text.toString())) {
            num2 = java.lang.Float.parseFloat(_number2.text.toString())
        }

        _resultEmitterSubject.onNext(num1 + num2)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _subscription.unSubscribeIfNotNull()
    }
}
