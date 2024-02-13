import TaskifyShared
import FirebaseMLModelDownloader

class iOSModelLoader : MlModelLoader {
    func loadClassifier(model: MlModel, completionHandler: @escaping (MlTFClassifier?, Error?) -> Void) {
        let conditions = ModelDownloadConditions(allowsCellularAccess: true)
        ModelDownloader.modelDownloader()
            .getModel(name: model.name,
                      downloadType: .latestModel,
                      conditions: conditions) { result in
                switch (result) {
                case .success(let customModel):
                    let path = customModel.path
                    let classifier = MlTFClassifier(modelPath: path, classes: model.classes)
                    completionHandler(classifier, nil)
                case .failure(let error):
                    completionHandler(nil, error)
                }
            }
    }
}
