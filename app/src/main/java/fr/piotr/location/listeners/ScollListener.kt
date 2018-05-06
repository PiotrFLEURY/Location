package fr.piotr.location.listeners

import android.content.Intent
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.widget.RecyclerView
import fr.piotr.location.fragments.EVENT_HIDE_FAB
import fr.piotr.location.fragments.EVENT_SHOW_FAB

class ScollListener : RecyclerView.OnScrollListener() {

    override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
        super.onScrollStateChanged(recyclerView, newState)
        when(newState){
            RecyclerView.SCROLL_STATE_DRAGGING -> LocalBroadcastManager.getInstance(recyclerView?.context).sendBroadcast(Intent(EVENT_HIDE_FAB))
            else -> LocalBroadcastManager.getInstance(recyclerView?.context).sendBroadcast(Intent(EVENT_SHOW_FAB))
        }
    }

}