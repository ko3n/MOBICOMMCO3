# CHOREO - Household Task Management Application

A modern Android application built with Kotlin and Jetpack Compose that helps households manage chores, track progress, and ensure fair task distribution among family members.

## Overview

CHOREO is a task management system designed specifically for households. It enables families to create tasks, assign them to members, track progress, and provide feedback on completed chores. The application features a beautiful UI, Firebase backend integration, and offline-first capabilities using Room database.

## Features

- User Registration and Authentication: Create household accounts with secure password hashing
- Task Management: Create, assign, and track household chores with priorities and due dates
- Recurring Tasks: Support for daily, weekly, monthly, and yearly recurring tasks
- Task Status Tracking: Tasks automatically categorized as Upcoming, Due Today, Overdue, or Completed
- Household Members: Manage multiple family members within a household
- Real-time Sync: Firebase Realtime Database integration for cloud synchronization
- Offline Support: Room database for local data persistence
- Onboarding Flow: Step-by-step user introduction to application features
- Material Design 3: Modern UI components and responsive layouts

## Tech Stack

- Language: Kotlin
- UI Framework: Jetpack Compose with Material Design 3
- Database: Room (SQLite)
- Backend: Firebase Realtime Database and Analytics
- Architecture: MVVM pattern with Repository pattern
- Async: Kotlin Coroutines
- Navigation: Jetpack Navigation Compose
- Build System: Gradle with Kotlin DSL
