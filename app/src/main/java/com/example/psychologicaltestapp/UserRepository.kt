package com.example.psychologicaltestapp

/**
 * Backwards compatibility wrapper so callers that used the legacy
 * `com.example.psychologicaltestapp.UserRepository` reference continue to
 * resolve to the consolidated profile repository implementation.
 */
typealias UserRepository = com.example.psychologicaltestapp.data.profile.UserRepository
