package com.example.rayment.model

/**
 * Response code:
 * 100:received
 * 200:success
 * 300:redirect
 * 400:Client side error
 * 500:Server error
 */
class ResponseDTO(//Error code
    var error: String?, //For display
    var message: String?, //For tracing error
    var detail: String?,
    var instance: String?, //
    var helpUrl: String?,
    var obj: Any?
)
