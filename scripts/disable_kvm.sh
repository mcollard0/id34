#!/bin/bash

# disable_kvm.sh - Temporarily disable KVM kernel modules
# Usage: sudo ./disable_kvm.sh

set -e

echo "🛑 Disabling KVM kernel modules..."

# Check if running as root
if [ "$EUID" -ne 0 ]; then
    echo "❌ This script must be run as root (use sudo)"
    exit 1
fi

# Check if any VMs are running
if pgrep -f "qemu\|kvm\|emulator" > /dev/null; then
    echo "⚠️  Warning: Found running virtual machines/emulators:"
    pgrep -af "qemu\|kvm\|emulator" || true
    echo ""
    read -p "Do you want to continue? This may kill running VMs [y/N]: " -r
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        echo "❌ Aborted by user"
        exit 1
    fi
fi

echo "📊 Current KVM modules:"
lsmod | grep kvm || echo "   No KVM modules currently loaded"
echo ""

# Remove KVM modules (order matters - remove specific modules first)
MODULES_TO_REMOVE=("kvm_intel" "kvm_amd" "kvm")

for module in "${MODULES_TO_REMOVE[@]}"; do
    if lsmod | grep -q "^$module "; then
        echo "📤 Removing module: $module"
        if ! rmmod "$module" 2>/dev/null; then
            echo "⚠️  Failed to remove $module (may be in use)"
            # Try to force remove if safe
            if [[ "$module" != "kvm" ]]; then
                echo "🔨 Attempting force removal of $module..."
                rmmod -f "$module" 2>/dev/null || echo "   Force removal also failed"
            fi
        else
            echo "✅ Successfully removed $module"
        fi
    else
        echo "ℹ️  Module $module not loaded"
    fi
done

# Verify removal
echo ""
echo "📊 Final KVM status:"
if lsmod | grep kvm; then
    echo "⚠️  Some KVM modules still loaded (may be in use)"
    echo "💡 Try stopping all VMs and emulators, then run this script again"
else
    echo "✅ All KVM modules successfully removed"
fi

# Check /dev/kvm status
if [ -c /dev/kvm ]; then
    echo "ℹ️  /dev/kvm device still exists (will be removed on next boot)"
else
    echo "✅ /dev/kvm device not present"
fi

echo ""
echo "🎯 KVM disable process completed"
echo "💡 Note: Changes are temporary - KVM will be available again after reboot"