// Some plugins expect to be declared exactly once...
plugins {
    alias(libs.plugins.jetbrains.compose) apply false
}