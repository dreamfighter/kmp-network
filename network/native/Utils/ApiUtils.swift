import Foundation

@objc public class ApiUtils : NSObject {

    @objc public class func getProperty(obj:Any) {

        print("items: \(type(of: obj))")
        let mirror = Mirror(reflecting: obj)
        print("\(mirror.children.count)")
        //let annotation = mirror.
        for (index, _) in mirror.children.enumerated() {
        print("label \(index)")
        }
        //print("Annotation value: \(Transaction(1))")
    }
}