package dev.chara.taskify.shared.domain

import dev.chara.taskify.shared.database.AccountManager
import dev.chara.taskify.shared.datastore.Preferences
import dev.chara.taskify.shared.domain.use_case.GetSelectedWorkspaceUseCase
import dev.chara.taskify.shared.domain.use_case.GetWorkspaceMembersUseCase
import dev.chara.taskify.shared.domain.use_case.SelectWorkspaceUseCase
import dev.chara.taskify.shared.domain.use_case.ShareWorkspaceUseCase
import dev.chara.taskify.shared.domain.use_case.account.GetCurrentProfileUseCase
import dev.chara.taskify.shared.domain.use_case.account.LinkFcmTokenUseCase
import dev.chara.taskify.shared.domain.use_case.account.RefreshDataUseCase
import dev.chara.taskify.shared.domain.use_case.account.SignInUseCase
import dev.chara.taskify.shared.domain.use_case.account.SignOutUseCase
import dev.chara.taskify.shared.domain.use_case.account.SignUpUseCase
import dev.chara.taskify.shared.domain.use_case.data.category.CreateCategoryUseCase
import dev.chara.taskify.shared.domain.use_case.data.category.DeleteCategoryUseCase
import dev.chara.taskify.shared.domain.use_case.data.category.GetCategoriesForWorkspaceUseCase
import dev.chara.taskify.shared.domain.use_case.data.category.GetCategoryUseCase
import dev.chara.taskify.shared.domain.use_case.data.category.UpdateCategoryUseCase
import dev.chara.taskify.shared.domain.use_case.data.category_prefs.CreateCategoryPrefsUseCase
import dev.chara.taskify.shared.domain.use_case.data.category_prefs.ReorderCategoriesUseCase
import dev.chara.taskify.shared.domain.use_case.data.category_prefs.UpdateCategoryPrefsUseCase
import dev.chara.taskify.shared.domain.use_case.data.task.CreateTaskUseCase
import dev.chara.taskify.shared.domain.use_case.data.task.DeleteAllCompletedTasksForCategoryUseCase
import dev.chara.taskify.shared.domain.use_case.data.task.DeleteTaskUseCase
import dev.chara.taskify.shared.domain.use_case.data.task.GetTaskUseCase
import dev.chara.taskify.shared.domain.use_case.data.task.UpdateAndMoveTaskUseCase
import dev.chara.taskify.shared.domain.use_case.data.task.UpdateTaskUseCase
import dev.chara.taskify.shared.domain.use_case.data.workspace.CreateWorkspaceUseCase
import dev.chara.taskify.shared.domain.use_case.data.workspace.DeleteWorkspaceUseCase
import dev.chara.taskify.shared.domain.use_case.data.workspace.GetAllWorkspacesUseCase
import dev.chara.taskify.shared.domain.use_case.data.workspace.GetWorkspaceUseCase
import dev.chara.taskify.shared.domain.use_case.data.workspace.UpdateWorkspaceUseCase
import dev.chara.taskify.shared.domain.use_case.details.GetCategoryDetailsUseCase
import dev.chara.taskify.shared.domain.use_case.details.GetTaskDetailsUseCase
import dev.chara.taskify.shared.domain.use_case.home.GetHomeContentUseCase
import dev.chara.taskify.shared.domain.use_case.home.GetReorderInfoUseCase
import dev.chara.taskify.shared.domain.use_case.home.GetSharingInfoUseCase
import dev.chara.taskify.shared.domain.use_case.membership.CreateInviteTokenUseCase
import dev.chara.taskify.shared.domain.use_case.membership.GetInviteDetailsUseCase
import dev.chara.taskify.shared.domain.use_case.membership.JoinWorkspaceUseCase
import dev.chara.taskify.shared.domain.use_case.membership.LeaveWorkspaceUseCase
import dev.chara.taskify.shared.domain.use_case.membership.RemoveMemberFromWorkspaceUseCase
import dev.chara.taskify.shared.domain.use_case.ml.ClassifyTaskUseCase
import dev.chara.taskify.shared.domain.use_case.permission.GetPermissionRequestHiddenUseCase
import dev.chara.taskify.shared.domain.use_case.permission.SetPermissionRequestHiddenUseCase
import dev.chara.taskify.shared.ml.ClassifierStore
import dev.chara.taskify.shared.network.gemini.GeminiApiClient
import org.koin.core.module.Module
import org.koin.dsl.module

internal expect fun Module.platform()

fun domain() = module {
    platform()

    single { AccountManager(BuildKonfig.atlasAppId) }
    single { GeminiApiClient(BuildKonfig.geminiApiKey) }
    single { ClassifierStore(get()) }
    single { Preferences(get()) }

    single { GetCurrentProfileUseCase(get()) }
    single { RefreshDataUseCase(get()) }
    single { SignInUseCase(get(), get(), get()) }
    single { SignUpUseCase(get(), get()) }
    single { SignOutUseCase(get()) }
    single { LinkFcmTokenUseCase(get()) }

    single { GetWorkspaceUseCase(get()) }
    single { GetAllWorkspacesUseCase(get()) }
    single { CreateWorkspaceUseCase(get(), get()) }
    single { UpdateWorkspaceUseCase(get()) }
    single { DeleteWorkspaceUseCase(get()) }

    single { GetCategoryUseCase(get()) }
    single { GetCategoriesForWorkspaceUseCase(get()) }
    single { CreateCategoryUseCase(get()) }
    single { UpdateCategoryUseCase(get()) }
    single { DeleteCategoryUseCase(get()) }

    single { CreateCategoryPrefsUseCase(get()) }
    single { UpdateCategoryPrefsUseCase(get()) }
    single { ReorderCategoriesUseCase(get()) }

    single { GetTaskUseCase(get()) }
    single { CreateTaskUseCase(get()) }
    single { UpdateTaskUseCase(get()) }
    single { UpdateAndMoveTaskUseCase(get()) }
    single { DeleteTaskUseCase(get()) }
    single { DeleteAllCompletedTasksForCategoryUseCase(get()) }

    single { GetWorkspaceMembersUseCase(get()) }
    single { GetSelectedWorkspaceUseCase(get()) }
    single { SelectWorkspaceUseCase(get()) }
    single { ShareWorkspaceUseCase(get()) }

    single { GetHomeContentUseCase(get(), get(), get()) }
    single { GetReorderInfoUseCase(get()) }
    single { GetSharingInfoUseCase(get()) }

    single { GetCategoryDetailsUseCase(get(), get()) }
    single { GetTaskDetailsUseCase(get(), get()) }

    single { CreateInviteTokenUseCase(get()) }
    single { GetInviteDetailsUseCase(get()) }
    single { JoinWorkspaceUseCase(get()) }
    single { LeaveWorkspaceUseCase(get()) }
    single { RemoveMemberFromWorkspaceUseCase(get()) }

    single { ClassifyTaskUseCase(get(), get()) }

    single { GetPermissionRequestHiddenUseCase(get()) }
    single { SetPermissionRequestHiddenUseCase(get()) }
}