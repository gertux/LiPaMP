# Linux Packaging Maven Plugin

The Linux Packaging Maven Plugin aims to build Linux Distribution specific packages without the use of native 
packaging tools, thus avoiding the use of non Java binaries and scripts.

The plugin focuses on the packaging of Java artifacts, for instance Spring boot JARs, WAR's to be deployed with 
a servlet runner like Tomcat or Jetty, ...
  
In the (near) future the plugin will be able to generate RPM (RedHat, SuSe) and APK (Arch, Alpine) packages but 
for the moment only the DEB (Debian, Ubuntu) format is implemented.
  
The plugin documentation can be found at <http://www.hobbiton.be/docs/linux-packaging-maven-plugin/>