package dev.maharshpatel.atmapp.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.maharshpatel.atmapp.InsufficientNotesException
import dev.maharshpatel.atmapp.Repository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ATMActivityViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = Repository(application, viewModelScope)
    private val _atmMoneyStateFlow = MutableStateFlow(Pair(mapOf(0 to 0), 0))
    val atmMoney = _atmMoneyStateFlow.asStateFlow()
    private val _currentTransactionStateFlow = MutableStateFlow(Pair(mapOf(0 to 0), 0))
    val currentTransactionStateFlow = _currentTransactionStateFlow.asStateFlow()
    private val _previousTransactionStateFlow =
        MutableStateFlow(listOf<Pair<Map<Int, Int>, Int>>())
    val previousTransactionStateFlow = _previousTransactionStateFlow.asStateFlow()
    private val _atmEventsSharedFlow = MutableSharedFlow<ATMActivityEvents>()
    val atmEvents = _atmEventsSharedFlow.asSharedFlow()

    private var availableAmountAndCurrencyCount = Pair(mapOf(0 to 0), 0)

    fun getData() {
        viewModelScope.launch {
            repository.getATMCurrency().collect {
                availableAmountAndCurrencyCount = it
                _atmMoneyStateFlow.emit(availableAmountAndCurrencyCount)
            }
        }
        viewModelScope.launch {
            repository.getTransactionHistory().collect {
                _previousTransactionStateFlow.emit(it)
            }
        }
    }

    fun withdrawBtnClicked(amount: Int) = viewModelScope.launch {
        if (amount > availableAmountAndCurrencyCount.second) {
            _atmEventsSharedFlow.emit(ATMActivityEvents.ShowErrorMessage("Input valid amount"))
            return@launch
        }
        repository.withdraw(amount).onSuccess {
            _currentTransactionStateFlow.emit(it)
        }.onFailure {
            if (it is InsufficientNotesException)
                _atmEventsSharedFlow.emit(ATMActivityEvents.ShowErrorMessage("No notes available to handle the transaction"))
        }
    }


    sealed class ATMActivityEvents {
        data class ShowErrorMessage(val message: String) : ATMActivityEvents()
    }


}