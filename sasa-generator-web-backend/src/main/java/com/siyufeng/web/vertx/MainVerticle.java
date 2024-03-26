package com.siyufeng.web.vertx;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;

public class MainVerticle extends AbstractVerticle {
  @Override
  public void start() throws Exception {
    // 创建 HTTP 服务
    vertx.createHttpServer()
      // 使用路由处理所有请求
      .requestHandler(request -> {
        request.response().putHeader("Content-Type","text/plain;charset=UTF-8")
                .end("ok");
      })
      // 开始监听端口
      .listen(8888)
      // 打印监听的端口
      .onSuccess(server ->
        System.out.println(
          "HTTP server started on port " + server.actualPort()
        )
      );
  }


  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();
    MainVerticle mainVerticle = new MainVerticle();
    vertx.deployVerticle(mainVerticle);
  }
}