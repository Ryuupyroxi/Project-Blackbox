package com.blackbox.module.adt.bridge

import com.blackbox.core.module.contract.AdtReceiverDefinition
import com.blackbox.core.module.contract.AdtServiceDefinition
import com.blackbox.module.adt.service.AdtServiceCatalog

object AdtManifestMapper {
    fun services(): List<AdtServiceDefinition> = AdtServiceCatalog.services
    fun bootReceivers(): List<AdtReceiverDefinition> = AdtServiceCatalog.bootReceivers
}
