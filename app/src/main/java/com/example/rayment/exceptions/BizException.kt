package com.example.rayment.exceptions

import java.lang.Exception

class BizException(message: String?) : Exception(message) {
    constructor() : this("BizException")
}