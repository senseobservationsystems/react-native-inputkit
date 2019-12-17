require 'json'

package = JSON.parse(File.read('./package.json'))

Pod::Spec.new do |s|
  s.name         = "RNInputKit"
  s.version      = package["version"]
  s.summary      = package["description"]
  s.homepage     = "https://github.com/github_account/react-native-inputkit"
  s.license      = "MIT"
  # s.license    = { :type => "MIT", :file => "FILE_LICENSE" }
  s.authors      = { "Your Name" => "yourname@email.com" }
  s.platform    = :ios, "10.0" 
  s.ios.deployment_target = '10.0'
  # file:/Users/Xavi/Development/ReactNative/react-native-inputkit":
  # s.source            = { :http => 'file:' + __dir__ + '/' }
  s.source       = { :git => "file:///Users/Xavi/Development/ReactNative/react-native-inputkit/" }

  s.source_files = "ios/**/*.{h,m,swift}"
  s.requires_arc = true

  s.dependency "React"
  # ...
  # s.dependency "..."
end

