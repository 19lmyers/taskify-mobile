package dev.chara.taskify.shared.domain.use_case

import dev.chara.taskify.shared.domain.ShareSheetManager

class ShareWorkspaceUseCase(private val shareSheet: ShareSheetManager) {
    operator fun invoke(token: String) = shareSheet.shareList(token)
}