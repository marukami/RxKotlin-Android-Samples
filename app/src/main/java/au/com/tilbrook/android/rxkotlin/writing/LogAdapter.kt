package au.com.tilbrook.android.rxkotlin.writing

import android.content.Context
import android.widget.ArrayAdapter
import au.com.tilbrook.android.rxkotlin.R

class LogAdapter(context: Context, logs: List<String>) :
        ArrayAdapter<String>(context, R.layout.item_log, R.id.item_log, logs)