DESCRIPTION = "Linux kernel for the R-Car Generation 3 based board"

require include/avb-control.inc
require include/iccom-control.inc
require recipes-kernel/linux/linux-yocto.inc
require include/cas-control.inc
require include/adsp-control.inc

FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}/:"
COMPATIBLE_MACHINE = "salvator-x|h3ulcb|m3ulcb|ebisu"

RENESAS_BSP_URL = "git://git.kernel.org/pub/scm/linux/kernel/git/horms/renesas-bsp.git"
BRANCH = "v4.14.70-ltsi-rc1/rcar-3.8.0"
SRCREV = "52f8a317698424fe3a4ba7f88d2b87fc6bf6591d"

SRC_URI = "${RENESAS_BSP_URL};protocol=git;nocheckout=1;branch=${BRANCH}"

LINUX_VERSION ?= "4.14.70"
PV = "${LINUX_VERSION}+git${SRCPV}"
PR = "r1"

SRC_URI_append = " \
    file://defconfig \
    file://touch.cfg \
    ${@base_conditional("USE_AVB", "1", " file://usb-video-class.cfg", "", d)} \
"

# Enable RPMSG_VIRTIO depend on ICCOM
SUPPORT_ICCOM = " \
    file://0001-rpmsg-Add-message-to-be-able-to-configure-RPMSG_VIRT.patch \
    file://iccom.cfg \
"

SRC_URI_append = " \
    ${@base_conditional("USE_ICCOM", "1", "${SUPPORT_ICCOM}", "", d)} \
"

# Add SCHED_DEBUG config fragment to support CAS
SRC_URI_append = " \
    ${@base_conditional("USE_CAS", "1", " file://capacity_aware_migration_strategy.cfg", "",d)} \
"

# Device tree patches
SRC_URI_append = " \
    file://0001-arm64-dts-salvator-common-Rcar-Sound.patch \
    file://0002-arm64-dts-r8a7796-salvator-xs-Remove-Sound-Card.patch \
    file://0003-arm64-dts-r8a7795-salvator-x-Remove-Sound-Card.patch \
    file://0004-arm64-dts-r8a7795-salvator-xs-Remove-Sound-Card.patch \
    file://0005-arm64-dts-r8a7795-es1-salvator-x-Remove-Sound-Card.patch \
    file://0006-arm64-dts-r8a7796-salvator-x-Remove-Sound-Card.patch \
"

# Add ADSP ALSA driver
SUPPORT_ADSP_ASOC = " \
    file://0001-Add-build-for-ADSP-sound-driver.patch \
    file://0002-Add-document-file-for-ADSP-sound-driver.patch \
    file://0003-Add-ADSP-sound-driver-source.patch \
    file://0004-Update-device-tree-for-ADSP-sound-driver.patch \
    file://adsp.cfg \
"

SRC_URI_append = " \
    ${@base_conditional("USE_ADSP", "1", "${SUPPORT_ADSP_ASOC}", "", d)} \
"

# Install USB3.0 firmware to rootfs
USB3_FIRMWARE_V2 = "https://git.kernel.org/pub/scm/linux/kernel/git/firmware/linux-firmware.git/plain/r8a779x_usb3_v2.dlmem;md5sum=645db7e9056029efa15f158e51cc8a11"
USB3_FIRMWARE_V3 = "https://git.kernel.org/pub/scm/linux/kernel/git/firmware/linux-firmware.git/plain/r8a779x_usb3_v3.dlmem;md5sum=687d5d42f38f9850f8d5a6071dca3109"

SRC_URI_append = " \
    ${USB3_FIRMWARE_V2} \
    ${USB3_FIRMWARE_V3} \
    ${@bb.utils.contains('MACHINE_FEATURES','usb3','file://usb3.cfg','',d)} \
"

do_download_firmware () {
    install -m 755 ${WORKDIR}/r8a779x_usb3_v*.dlmem ${STAGING_KERNEL_DIR}/firmware
}

addtask do_download_firmware after do_configure before do_compile
