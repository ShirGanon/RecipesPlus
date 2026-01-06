package com.example.recipesplus.ui.recipes;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipesplus.R;
import com.example.recipesplus.services.SpoonacularService;

public class SearchOnlineFragment extends Fragment {

    private RecipeAdapter adapter;

    public SearchOnlineFragment() {
        super(R.layout.fragment_search_online);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        EditText etQuery = view.findViewById(R.id.et_query);
        RecyclerView rv = view.findViewById(R.id.rv_online);

        rv.setLayoutManager(new LinearLayoutManager(requireContext()));

        view.findViewById(R.id.btn_search).setOnClickListener(v -> {
            String q = etQuery.getText().toString().trim();
            if (q.isEmpty()) {
                Toast.makeText(requireContext(), "Enter search text", Toast.LENGTH_SHORT).show();
                return;
            }

            new SpoonacularService().search(requireContext(), q, new SpoonacularService.Callback() {
                @Override
                public void onSuccess(java.util.List<com.example.recipesplus.model.Recipe> recipes) {
                    requireActivity().runOnUiThread(() -> {
                        adapter = new RecipeAdapter(recipes, recipe ->
                                Toast.makeText(requireContext(), recipe.getTitle(), Toast.LENGTH_SHORT).show()
                        );
                        rv.setAdapter(adapter);
                    });
                }

                @Override
                public void onError(String message) {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(requireContext(), "Error: " + message, Toast.LENGTH_LONG).show()
                    );
                }
            });
        });
    }
}
