Pod::Spec.new do |spec|
    spec.name                     = 'TaskifyML'
    spec.version                  = '1.0.0'
    spec.homepage                 = 'https://taskify.chara.dev'
    spec.source                   = { :http=> ''}
    spec.authors                  = ''
    spec.license                  = 'All Rights Reserved'
    spec.summary                  = 'Machine Learning module for Taskify on Apple platforms'
    spec.vendored_frameworks      = 'build/cocoapods/framework/TaskifyML.framework'
    spec.libraries                = 'c++'
    spec.ios.deployment_target = '15.2'
    spec.dependency 'TensorFlowLiteObjC'
                
    if !Dir.exist?('build/cocoapods/framework/TaskifyML.framework') || Dir.empty?('build/cocoapods/framework/TaskifyML.framework')
        raise "

        Kotlin framework 'TaskifyML' doesn't exist yet, so a proper Xcode project can't be generated.
        'pod install' should be executed after running ':generateDummyFramework' Gradle task:

            ./gradlew :shared:ml:generateDummyFramework

        Alternatively, proper pod installation is performed during Gradle sync in the IDE (if Podfile location is set)"
    end
                
    spec.pod_target_xcconfig = {
        'KOTLIN_PROJECT_PATH' => ':shared:ml',
        'PRODUCT_MODULE_NAME' => 'TaskifyML',
    }
                
    spec.script_phases = [
        {
            :name => 'Build TaskifyML',
            :execution_position => :before_compile,
            :shell_path => '/bin/sh',
            :script => <<-SCRIPT
                if [ "YES" = "$OVERRIDE_KOTLIN_BUILD_IDE_SUPPORTED" ]; then
                  echo "Skipping Gradle build task invocation due to OVERRIDE_KOTLIN_BUILD_IDE_SUPPORTED environment variable set to \"YES\""
                  exit 0
                fi
                set -ev
                REPO_ROOT="$PODS_TARGET_SRCROOT"
                "$REPO_ROOT/../../gradlew" -p "$REPO_ROOT" $KOTLIN_PROJECT_PATH:syncFramework \
                    -Pkotlin.native.cocoapods.platform=$PLATFORM_NAME \
                    -Pkotlin.native.cocoapods.archs="$ARCHS" \
                    -Pkotlin.native.cocoapods.configuration="$CONFIGURATION"
            SCRIPT
        }
    ]
                
end