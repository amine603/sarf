package com.cash.guide.feature.cashregister

import androidx.lifecycle.ViewModel
import com.cash.guide.domain.MoneyMath
import com.cash.guide.domain.MoneyPiece
import com.cash.guide.domain.MoneyUnit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class CashRegisterUiState(
    val purchaseText: String = "",
    val receivedText: String = "",
    val currencyUnit: MoneyUnit = MoneyUnit.DIRHAM,
    val purchaseCentimes: Long = 0L,
    val receivedCentimes: Long = 0L,
    val changeCentimes: Long = 0L,
    val shortageCentimes: Long = 0L,
    val isExactAmount: Boolean = false,
    val isInsufficient: Boolean = false,
    val pieces: List<MoneyPiece> = emptyList(),
    val isPurchaseValid: Boolean = true,
    val isReceivedValid: Boolean = true
)

class CashRegisterViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(CashRegisterUiState())
    val uiState: StateFlow<CashRegisterUiState> = _uiState.asStateFlow()

    fun setPurchaseText(text: String) {
        _uiState.update { current ->
            computeState(
                purchaseText = text,
                receivedText = current.receivedText,
                currencyUnit = current.currencyUnit
            )
        }
    }

    fun setReceivedText(text: String) {
        _uiState.update { current ->
            computeState(
                purchaseText = current.purchaseText,
                receivedText = text,
                currencyUnit = current.currencyUnit
            )
        }
    }

    fun selectPresetReceived(dh: Long) {
        val currentUnit = _uiState.value.currencyUnit
        val amountText = if (currentUnit == MoneyUnit.DIRHAM) {
            dh.toString()
        } else {
            // Convert dh to rial (1 DH = 20 RIAL)
            (dh * 20).toString()
        }
        setReceivedText(amountText)
    }

    fun toggleCurrency() {
        _uiState.update { current ->
            val nextUnit = if (current.currencyUnit == MoneyUnit.DIRHAM) MoneyUnit.RIAL else MoneyUnit.DIRHAM
            val convertedPurchase = MoneyMath.convertExpression(current.purchaseText, current.currencyUnit, nextUnit)
            val convertedReceived = MoneyMath.convertExpression(current.receivedText, current.currencyUnit, nextUnit)
            computeState(
                purchaseText = convertedPurchase,
                receivedText = convertedReceived,
                currencyUnit = nextUnit
            )
        }
    }

    fun clear() {
        _uiState.update { current ->
            CashRegisterUiState(currencyUnit = current.currencyUnit)
        }
    }

    private fun computeState(
        purchaseText: String,
        receivedText: String,
        currencyUnit: MoneyUnit
    ): CashRegisterUiState {
        val purchaseTrimmed = purchaseText.trim()
        val receivedTrimmed = receivedText.trim()

        val isPurchaseValid = purchaseTrimmed.isEmpty() || MoneyMath.isValidExpression(purchaseTrimmed)
        val isReceivedValid = receivedTrimmed.isEmpty() || MoneyMath.isValidExpression(receivedTrimmed)

        val purchaseCentimes = if (purchaseTrimmed.isNotEmpty() && isPurchaseValid) {
            MoneyMath.toCentimes(purchaseTrimmed, currencyUnit) ?: 0L
        } else 0L

        val receivedCentimes = if (receivedTrimmed.isNotEmpty() && isReceivedValid) {
            MoneyMath.toCentimes(receivedTrimmed, currencyUnit) ?: 0L
        } else 0L

        val hasBoth = purchaseTrimmed.isNotEmpty() && receivedTrimmed.isNotEmpty() && isPurchaseValid && isReceivedValid

        val changeCentimes: Long
        val shortageCentimes: Long
        val isExactAmount: Boolean
        val isInsufficient: Boolean
        val pieces: List<MoneyPiece>

        if (hasBoth && purchaseCentimes > 0L) {
            when {
                receivedCentimes > purchaseCentimes -> {
                    changeCentimes = receivedCentimes - purchaseCentimes
                    shortageCentimes = 0L
                    isExactAmount = false
                    isInsufficient = false
                    pieces = MoneyMath.breakdown(changeCentimes)
                }
                receivedCentimes == purchaseCentimes -> {
                    changeCentimes = 0L
                    shortageCentimes = 0L
                    isExactAmount = true
                    isInsufficient = false
                    pieces = emptyList()
                }
                else -> {
                    changeCentimes = 0L
                    shortageCentimes = purchaseCentimes - receivedCentimes
                    isExactAmount = false
                    isInsufficient = true
                    pieces = emptyList()
                }
            }
        } else {
            changeCentimes = 0L
            shortageCentimes = 0L
            isExactAmount = false
            isInsufficient = false
            pieces = emptyList()
        }

        return CashRegisterUiState(
            purchaseText = purchaseText,
            receivedText = receivedText,
            currencyUnit = currencyUnit,
            purchaseCentimes = purchaseCentimes,
            receivedCentimes = receivedCentimes,
            changeCentimes = changeCentimes,
            shortageCentimes = shortageCentimes,
            isExactAmount = isExactAmount,
            isInsufficient = isInsufficient,
            pieces = pieces,
            isPurchaseValid = isPurchaseValid,
            isReceivedValid = isReceivedValid
        )
    }
}
