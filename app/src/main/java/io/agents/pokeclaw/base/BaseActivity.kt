// Copyright 2026 PokeClaw (agents.io). All rights reserved.
// Licensed under the Apache License, Version 2.0.

package io.agents.pokeclaw.base

import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.blankj.utilcode.util.AdaptScreenUtils
import com.blankj.utilcode.util.BarUtils
import io.agents.pokeclaw.R

/**
 *
 * Screen adaptation uses pt; using dp on some devices causes toast line breaks at incorrect positions
 */
open class BaseActivity : AppCompatActivity() {

    override fun getResources(): Resources {
        val resources = super.getResources()
        return if (resources.configuration.screenWidthDp < 600) AdaptScreenUtils.adaptWidth(resources, getDesignWidth()) else resources
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.enableEdgeToEdge(window)
        applyStatusBarMode()

        // Handle status bar height uniformly - applied after layout is loaded
        applyStatusBarPadding()
    }

    /**
     * Auto-callback on theme switch to update status bar text color
     */
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        applyStatusBarMode()
    }

    /**
     * Set status bar text color based on current theme mode
     * Light theme → dark text (light mode)
     * Dark theme → light text (dark mode)
     */
    private fun applyStatusBarMode() {
        val isNightMode = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        BarUtils.setStatusBarLightMode(this, !isNightMode)
    }

    /**
     * Add top padding equal to the status bar height to the root view
     * Subclasses can disable this by overriding isApplyStatusBarPadding()
     */
    private fun applyStatusBarPadding() {
        window.decorView.post {
            val rootView = findViewById<ViewGroup>(android.R.id.content)?.getChildAt(0)
            rootView?.let { applyPaddingToRootView(it); ViewCompat.requestApplyInsets(it) }
        }
    }

    /**
     * Apply status bar height padding to the root view
     * If the layout already handles insets, override this method to customize behavior
     */
    protected open fun applyPaddingToRootView(rootView: View) {
        if (!isApplyStatusBarPadding()) return

        val initialTop = rootView.paddingTop
        val initialBottom = rootView.paddingBottom
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { view, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.statusBars() or
                    WindowInsetsCompat.Type.navigationBars() or
                    WindowInsetsCompat.Type.displayCutout()
            )
            view.updatePadding(
                top = initialTop + bars.top,
                bottom = initialBottom + bars.bottom,
            )
            insets
        }
    }

    /**
     * Subclasses can override this method to disable automatic status bar height padding
     */
    protected open fun isApplyStatusBarPadding(): Boolean = true

    /**
     * Screen adaptation - design spec size
     */
    open fun getDesignWidth(): Int {
        return 402
    }
}