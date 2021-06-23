package com.afsar.bottomdialog.fragments.nestedfragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import com.afsar.bottomdialog.R
import com.afsar.bottomdialog.fragments.AFragment

class NestedFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_nested, container, false)
    }

    companion object {
        private const val POS = "pos"

        fun newInstance(pos: Int) = NestedFragment().apply {
            arguments = bundleOf(
                POS to pos
            )
        }
    }
}