package br.pedroso.remotemediatortest

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import br.pedroso.remotemediatortest.adapters.ItemsAdapter
import br.pedroso.remotemediatortest.adapters.ItemsLoadStateAdapter
import br.pedroso.remotemediatortest.databinding.ActivityMainBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val itemsAdapter = ItemsAdapter()

    private val viewModel: MainActivityViewModel by viewModels {
        MainActivityViewModelFactory()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setupViews()
        observePagingFlow()
    }

    private fun setupViews() = with(binding) {
        refreshButton.setOnClickListener { itemsAdapter.refresh() }

        itemsRecyclerView.apply {
            adapter = itemsAdapter.withLoadStateFooter(ItemsLoadStateAdapter(itemsAdapter::retry))
            addItemDecoration(DividerItemDecoration(context, RecyclerView.VERTICAL))
        }
    }

    private fun observePagingFlow() {
        lifecycleScope.launch {
            viewModel.pagingDataFlow.collectLatest { pagingData ->
                itemsAdapter.submitData(pagingData)
            }
        }
    }
}
