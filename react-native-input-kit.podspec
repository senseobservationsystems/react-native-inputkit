require 'json'

package = JSON.parse(File.read('./package.json'))

Pod::Spec.new do |s|
  s.name         = "react-native-input-kit"
  s.version      = package["version"]
  s.summary      = package["description"]
  s.homepage     = "https://github.com/github_account/react-native-input-kit"
  s.license      = "MIT"
  # s.license    = { :type => "MIT", :file => "FILE_LICENSE" }
  s.authors      = { "Your Name" => "yourname@email.com" }
  s.platform    = :ios, "9.0" 
  s.ios.deployment_target = '9.0'
  # file:/Users/Xavi/Development/ReactNative/react-native-input-kit":
  # s.source            = { :http => 'file:' + __dir__ + '/' }
  s.source       = { :git => "file:///Users/Xavi/Development/ReactNative/react-native-input-kit/" }

  s.source_files = "ios/**/*.{h,m,swift}"
  s.requires_arc = true

  s.dependency "React"
  # ...
  # s.dependency "..."
end

