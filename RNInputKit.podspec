require 'json'

package = JSON.parse(File.read('./package.json'))

Pod::Spec.new do |s|
  s.name                    = "RNInputKit"
  s.version                 = package["version"]
  s.summary                 = package["description"]
  s.homepage                = package["homepage"]
  s.license                 = package["license"]
  s.authors                 = { "Sense Health" => "xavier@sense-os.nl" }
  s.platform                = :ios, "10.0" 
  s.ios.deployment_target   = '10.0'
  
  s.source                  = { :git => "https://github.com/senseobservationsystems/react-native-inputkit.git", :tag => "v#{s.version}" }
  s.source_files            = "ios/**/*.{h,m,swift}"
  s.requires_arc            = true

  s.dependency "React"
  # ...
  # s.dependency "..."
end

