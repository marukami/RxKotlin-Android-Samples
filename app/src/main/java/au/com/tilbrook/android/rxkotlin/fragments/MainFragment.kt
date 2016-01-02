package au.com.tilbrook.android.rxkotlin.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import au.com.tilbrook.android.rxkotlin.R
import org.jetbrains.anko.*
import org.jetbrains.anko.support.v4.ctx

class MainFragment : BaseFragment() {

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        return with(ctx) {
            scrollView {
                lparams {
                    width = matchParent
                    height = matchParent
                }

                verticalLayout {
                    button (R.string.btn_demo_schedulers) {
                        style { R.style.buttons }
                        onClick {
                            clickedOn( ConcurrencyWithSchedulersDemoFragment() )
                        }
                    }
                    button (R.string.btn_demo_buffer) {
                        style { R.style.buttons }
                        onClick {
                            clickedOn ( BufferDemoFragment() )
                        }
                    }
                    button (R.string.btn_demo_debounce) {
                        style { R.style.buttons }
                        onClick {
                            clickedOn(DebounceSearchEmitterFragment())
                        }
                    }
                    button (R.string.btn_demo_retrofit) {
                        style { R.style.buttons }
                        onClick {
//                            clickedOn(RetrofitFragment())
                        }
                    }
                    button (R.string.btn_demo_double_binding_textview) {
                        style { R.style.buttons }
                        onClick {
//                            clickedOn(DoubleBindingTextViewFragment())
                        }
                    }
                    button (R.string.btn_demo_rxbus) {
                        style { R.style.buttons }
                        onClick {
//                            clickedOn(new RxBusDemoFragment())
                        }
                    }
                    button (R.string.btn_demo_form_validation_combinel) {
                        style { R.style.buttons }
                        onClick {
//                            clickedOn(FormValidationCombineLatestFragment())
                        }
                    }
                    button (R.string.btn_demo_pseudocache) {
                        style { R.style.buttons }
                        onClick {
//                            clickedOn(PseudoCacheMergeFragment())
                        }
                    }
                    button (R.string.btn_demo_timing) {
                        style { R.style.buttons }
                        onClick {
//                            clickedOn(TimingDemoFragment())
                        }
                    }
                    button (R.string.btn_demo_exponential_backoff) {
                        style { R.style.buttons }
                        onClick {
//                            clickedOn(ExponentialBackoffFragment())
                        }
                    }
                    button (R.string.btn_demo_rotation_persist) {
                        style { R.style.buttons }
                        onClick {
//                            clickedOn(RotationPersist2Fragment())
                        }
                    }
                }
            }
        }
    }

    fun clickedOn(fragment: Fragment) {
        val tag = fragment.javaClass.toString()
         activity.supportFragmentManager
                .beginTransaction()
                .addToBackStack(tag)
                .replace(android.R.id.content, fragment, tag)
                .commit();
    }
}