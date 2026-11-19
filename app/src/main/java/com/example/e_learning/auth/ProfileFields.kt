package com.example.e_learning.auth

import androidx.core.view.children
import com.example.e_learning.R
import com.example.e_learning.databinding.LayoutProfileFieldsBinding
import com.google.android.material.chip.Chip

class ProfileFields(private val b: LayoutProfileFieldsBinding) {

    var level: String
        get() = when (b.chipsLevel.checkedChipId) {
            R.id.chipIntermediate -> Level.INTERMEDIATE
            R.id.chipAdvanced -> Level.ADVANCED
            else -> Level.BEGINNER
        }
        set(value) = b.chipsLevel.check(
            when (value) {
                Level.INTERMEDIATE -> R.id.chipIntermediate
                Level.ADVANCED -> R.id.chipAdvanced
                else -> R.id.chipBeginner
            }
        )

    var languages: List<String>
        get() = b.chipsLanguages.children.filterIsInstance<Chip>()
            .filter { it.isChecked }.map { it.text.toString() }.toList()
        set(value) = b.chipsLanguages.children.filterIsInstance<Chip>()
            .forEach { it.isChecked = it.text.toString() in value }
}