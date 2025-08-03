package com.mobdeve.s16.group6.data

import com.google.firebase.database.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.Flow

class FirebaseFeedRepo {

    private val db: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val feedRoot: DatabaseReference = db.getReference("feeds")

    fun listenToFeed(householdId: String): Flow<List<FeedEvent>> = callbackFlow {
        val feedRef = feedRoot.child(householdId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val events = mutableListOf<FeedEvent>()
                for (child in snapshot.children) {
                    val event = child.getValue(FeedEvent::class.java)
                    if (event != null) events.add(event)
                }
                trySend(events.sortedByDescending { it.timestamp })
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        feedRef.addValueEventListener(listener)
        awaitClose { feedRef.removeEventListener(listener) }
    }

    suspend fun pushFeedEvent(householdId: String, event: FeedEvent) {
        feedRoot.child(householdId).push().setValue(event)
    }
}
