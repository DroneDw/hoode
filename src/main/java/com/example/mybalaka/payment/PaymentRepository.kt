package com.example.mybalaka.payment

class PaymentRepository {
    private val api = PayChanguApi.create()

    suspend fun startPayment(req: PayRequest) =
        api.pay(req)

    suspend fun checkStatus(id: String) =
        api.status(id)
}
