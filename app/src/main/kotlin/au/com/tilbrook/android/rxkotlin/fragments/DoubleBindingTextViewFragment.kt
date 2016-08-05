package au.com.tilbrook.android.rxkotlin.fragments

import android.os.Bundle
import android.text.InputType.TYPE_CLASS_NUMBER
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

        val listener = __TextWatcher()

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

                    orientation = HORIZONTAL
                    _number1 = editText("100") {
                        lparams(width = 0, height = dip(50), weight = 1f)
                        inputType = TYPE_CLASS_NUMBER or TYPE_NUMBER_FLAG_DECIMAL
                        addTextChangedListener(listener)
                    }
                    _number1.gravity = CENTER
                    textView("+") {
                        lparams(width = 0, height = dip(50), weight = 1f)
                    }.gravity = CENTER
                    _number2 = editText("8") {
                        lparams(width = 0, height = dip(50), weight = 1f)
                        inputType = TYPE_CLASS_NUMBER or TYPE_NUMBER_FLAG_DECIMAL
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

        listener.afterTextChanged { onNumberChanged()}
        onNumberChanged()
        _number2.requestFocus()

        return layout
    }

    //    @OnTextChanged(R.id.double_binding_num1, R.id.double_binding_num2)
    fun onNumberChanged() {

        val num1str = _number1.text.toString()
        val num1 = if (!isEmpty(num1str)) num1str.toFloat() else 0f
        val num2str = _number2.text.toString()
        val num2 = if (!isEmpty(num2str)) num2str.toFloat() else 0f

        _resultEmitterSubject.onNext(num1 + num2)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _subscription.unSubscribeIfNotNull()
    }
}
