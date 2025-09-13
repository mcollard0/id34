#!/bin/bash

# enable_kvm.sh - Temporarily enable KVM kernel modules for Android emulation
# Usage: sudo ./enable_kvm.sh

set -e

echo "🚀 Enabling KVM kernel modules..."

# Check if running as root
if [ "$EUID" -ne 0 ]; then
    echo "❌ This script must be run as root (use sudo)"
    exit 1
fi

# Check if Intel VT-x or AMD-V is available
if ! grep -E "(vmx|svm)" /proc/cpuinfo > /dev/null; then
    echo "❌ CPU does not support virtualization extensions (Intel VT-x or AMD-V)"
    echo "   Check BIOS settings to enable virtualization"
    exit 1
fi

# Detect CPU vendor
CPU_VENDOR=$(grep -m1 "vendor_id" /proc/cpuinfo | awk '{print $3}')

echo "🔍 CPU Vendor: $CPU_VENDOR"

# Load appropriate KVM modules
case "$CPU_VENDOR" in
    "GenuineIntel")
        echo "📦 Loading Intel KVM modules..."
        modprobe kvm
        modprobe kvm_intel
        ;;
    "AuthenticAMD")
        echo "📦 Loading AMD KVM modules..."
        modprobe kvm
        modprobe kvm_amd
        ;;
    *)
        echo "❌ Unknown CPU vendor: $CPU_VENDOR"
        exit 1
        ;;
esac

# Set permissions for KVM device
if [ -c /dev/kvm ]; then
    echo "🔧 Setting KVM device permissions..."
    chmod 666 /dev/kvm
    chown root:kvm /dev/kvm 2>/dev/null || true
    
    # Add current user to kvm group if exists
    KVM_GROUP=$(getent group kvm 2>/dev/null || true)
    if [ -n "$KVM_GROUP" ]; then
        ORIGINAL_USER=${SUDO_USER:-$USER}
        usermod -a -G kvm "$ORIGINAL_USER" 2>/dev/null || true
        echo "👤 Added user $ORIGINAL_USER to kvm group"
    fi
else
    echo "❌ /dev/kvm device not found after loading modules"
    exit 1
fi

# Verify KVM is working
if [ -r /dev/kvm ] && [ -w /dev/kvm ]; then
    echo "✅ KVM enabled successfully!"
    echo "📊 KVM Status:"
    lsmod | grep kvm
    ls -la /dev/kvm
else
    echo "❌ KVM device not accessible"
    exit 1
fi

echo ""
echo "🎯 KVM is now enabled for Android emulation"
echo "💡 Note: You may need to log out and back in for group membership to take effect"