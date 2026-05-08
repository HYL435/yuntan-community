import asyncio


# 并发限制器
class ConcurrencyLimiter:
    """
    Python AI服务并发保护器
    用来限制同一时间最多有多少个模型流式在线程在运行，防止过多的并发请求导致系统资源耗尽。
    """

    def __init__(self, max_concurrent: int):
        # 信号灯，用于控制并发数量，max_concurrent表示最大并发数
        # 当一个线程需要执行时，它必须先获取信号灯，如果当前并发数已经达到max_concurrent，那么这个线程就会被阻塞，直到有其他线程释放信号灯。
        self.semaphore = asyncio.Semaphore(max_concurrent)

    # 获取信号灯。acquire：获得
    async def acquire(self):

        # 如果信号灯被锁住了，说明当前并发数已经达到了最大值，无法再接受新的请求，直接返回False
        if self.semaphore.locked():
            return False

        # 否则，尝试获取信号灯，成功则返回True，失败则返回False
        await self.semaphore.acquire()
        return True

    def release(self):
        """
        释放信号灯。release：释放
        """
        self.semaphore.release()