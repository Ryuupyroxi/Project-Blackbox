package com.blackbox.module.adt.bridge

import com.blackbox.core.module.adt.model.AdtReceiverDefinition
import com.blackbox.core.module.adt.model.AdtServiceDefinition
import com.blackbox.core.module.adt.service.AdtServiceCatalog

object AdtManifestMapper {
    fun services(): List<AdtServiceDefinition> = AdtServiceCatalog.services
    fun bootReceivers(): List<AdtReceiverDefinition> = AdtServiceCatalog.bootReceivers
}
