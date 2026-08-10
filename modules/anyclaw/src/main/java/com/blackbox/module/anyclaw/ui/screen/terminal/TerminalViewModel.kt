package com.blackbox.module.anyclaw.ui.screen.terminal

import androidx.lifecycle.ViewModel
import com.blackbox.core.module.ModuleBus

class TerminalViewModel : ViewModel() {
    fun startProotShell() {
        ModuleBus.publish(ModuleEvent("anyclaw", "start_proot_shell"))
    }

    fun stopProotShell() {
        ModuleBus.publish(ModuleEvent("anyclaw", "stop_proot_shell"))
    }
}
