package dev.chara.taskify.shared.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import dev.chara.taskify.shared.domain.use_case.GetProfileImageUseCase
import dev.chara.taskify.shared.model.Profile

@Composable
fun ProfilePhoto(profile: Profile?) {
    if (profile == null) {
        Icon(Icons.Filled.AccountCircle, contentDescription = null)
    } else {
        val imageUrl = remember { GetProfileImageUseCase(profile.email) }

        Box(modifier = Modifier.padding(2.dp)) {
            SubcomposeAsyncImage(
                model = imageUrl,
                contentDescription = profile.email,
                modifier = Modifier.requiredSize(32.dp).clip(CircleShape),
                loading = {
                    ProfilePhotoFallback(profile)
                },
                error = {
                    ProfilePhotoFallback(profile)
                }
            )
        }
    }
}

@Composable
fun SelectedProfilePhoto() {
    Box(
        modifier = Modifier.padding(2.dp).clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary)
            .requiredSize(32.dp)
    ) {
        Icon(
            Icons.Filled.Check,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
private fun ProfilePhotoFallback(profile: Profile) {
    Box(
        modifier = Modifier.clip(CircleShape)
            .background(MaterialTheme.colorScheme.tertiaryContainer)
    ) {
        Text(
            modifier = Modifier.align(Alignment.Center),
            text = profile.email.take(2).uppercase(),
            style = MaterialTheme.typography.labelMedium,
        )
    }
}