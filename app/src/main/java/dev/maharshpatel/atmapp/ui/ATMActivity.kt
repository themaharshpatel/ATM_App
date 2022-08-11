package dev.maharshpatel.atmapp.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import dev.maharshpatel.atmapp.databinding.ActivityMainBinding
import dev.maharshpatel.atmapp.databinding.TableRowBinding


class ATMActivity : AppCompatActivity() {

    private val viewModel: ATMActivityViewModel by viewModels()
    private var binding: ActivityMainBinding? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)


        binding?.let {
            it.withdrawBtn.setOnClickListener { _ ->
                val amount = try {
                    it.amountInputEditText.text?.toString()?.toInt() ?: 0
                } catch (e: NumberFormatException) {
                    0
                }
                viewModel.withdrawBtnClicked(amount)

            }
        }


        collectFlowOnCreated(viewModel.atmMoney) { data ->
            binding?.let {
                it.atmAmountTbl.removeAllViews()
                it.atmAmountTbl.addView(getHeadRow("ATM Amount"))
                it.atmAmountTbl.addView(getRowData(data))
            }
        }

        collectFlowOnCreated(viewModel.atmEvents) { events ->
            when (events) {
                is ATMActivityViewModel.ATMActivityEvents.ShowErrorMessage -> {
                    Toast.makeText(this, events.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        collectFlowOnCreated(viewModel.currentTransactionStateFlow) { data ->
            binding?.let {
                it.withdrawnTbl.removeAllViews()
                it.withdrawnTbl.addView(getHeadRow("Withdrawn Amount"))
                it.withdrawnTbl.addView(getRowData(data))
            }
        }

        collectFlowOnCreated(viewModel.previousTransactionStateFlow) { historyList ->
            if (historyList.isEmpty())
                return@collectFlowOnCreated
            binding?.let {
                it.transactionHistTbl.removeAllViews()
                it.transactionHistTbl.addView(getHeadRow("Withdrawn Amount"))
                for (data in historyList)
                    it.transactionHistTbl.addView(getRowData(data))
            }
        }

        viewModel.getData()
    }

    @SuppressLint("SetTextI18n")
    private fun getRowData(data: Pair<Map<Int, Int>, Int>) =
        TableRowBinding.inflate(layoutInflater).apply {
            val map = data.first
            totalAmountCell.text = "Rs. ${data.second}"
            rs100Cell.text = map.getOrDefault(100, 0).toString()
            rs200Cell.text = map.getOrDefault(200, 0).toString()
            rs500Cell.text = map.getOrDefault(500, 0).toString()
            rs2000Cell.text = map.getOrDefault(2000, 0).toString()
        }.root

    @SuppressLint("SetTextI18n")
    private fun getHeadRow(title: String) =
        TableRowBinding.inflate(layoutInflater).apply {
            totalAmountCell.text = title
            rs100Cell.text = "Rs. 100"
            rs200Cell.text = "Rs. 200"
            rs500Cell.text = "Rs. 500"
            rs2000Cell.text = "Rs. 2000"
        }.root
}
