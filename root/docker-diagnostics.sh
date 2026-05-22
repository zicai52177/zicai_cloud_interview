#!/bin/bash
# Docker 服务诊断脚本

echo "=============================================="
echo "Docker 服务诊断报告"
echo "时间: $(date '+%Y-%m-%d %H:%M:%S')"
echo "=============================================="

echo ""
echo "[1] 检查 Docker 服务状态..."
sudo systemctl status docker

echo ""
echo "[2] Docker 服务最近日志 (50行)..."
sudo journalctl -u docker --no-pager -n 50

echo ""
echo "[3] 查看所有容器 (包括已停止)..."
sudo docker ps -a

echo ""
echo "[4] Docker 详细信息..."
sudo docker info

echo ""
echo "[5] 检查 Docker Compose 服务状态..."
cd /root/cloud_interview 2>/dev/null || cd ~
docker compose ps -a

echo ""
echo "[6] Docker 镜像列表..."
sudo docker images

echo ""
echo "[7] Docker 磁盘使用情况..."
sudo docker system df

echo "=============================================="
echo "诊断完成"
echo "=============================================="