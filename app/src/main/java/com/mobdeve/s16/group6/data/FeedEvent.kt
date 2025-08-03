package com.mobdeve.s16.group6.data

data class FeedEvent(
    val timestamp: Long = 0L,
    val taskTitle: String = "",
    val userName: String = "",
    val eventType: EventType = EventType.CREATED
)

enum class EventType {
    CREATED,
    MODIFIED,
    DELETED,
    COMPLETED
}