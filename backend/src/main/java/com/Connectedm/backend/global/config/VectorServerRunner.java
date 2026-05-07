package com.Connectedm.backend.global.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

@Component
public class VectorServerRunner {

    private Process serverProcess;      // vector_server.py (상주)
    //private Process fillerProcess;      // db_filler.py (일회성)
    //private Process analysisProcess;    // gemini_analysis.py (일회성)

    @PostConstruct
    public void startPythonAutomation() {
        try {
            // 1. 경로 자동 설정
            String rootPath = System.getProperty("user.dir");
            String workingDirPath = rootPath + File.separator + "data_crawling";
            String pythonExePath = workingDirPath + File.separator + ".venv" + File.separator + "Scripts" + File.separator + "python.exe";

            String serverScript = workingDirPath + File.separator + "vector_server.py";
            String fillerScript = workingDirPath + File.separator + "db_filler.py";
            String analysisScript = workingDirPath + File.separator + "gemini_analysis.py";

            // 2. 가상환경 체크
            File pythonExe = new File(pythonExePath);
            if (!pythonExe.exists()) {
                System.err.println("⚠️ [경고] .venv를 찾을 수 없습니다.");
                return;
            }

//            // [1단계] 데이터 주입기 먼저 실행! (빈 접시 깔기)
//            ProcessBuilder fillerPb = new ProcessBuilder(pythonExePath, fillerScript);
//            fillerPb.directory(new File(workingDirPath));
//            fillerPb.environment().put("PYTHONIOENCODING", "UTF-8");
//            fillerPb.inheritIO();
//            this.fillerProcess = fillerPb.start();
//            System.out.println("📦 [시스템] 보급반 가동: DB에 빈 접시를 깝니다...");
//
//            // 보급이 끝날 때까지 자바 서버 잠시 대기 (필수!)
//            this.fillerProcess.waitFor();
//            System.out.println("✅ [시스템] 보급 완료! 분석기를 호출합니다.");
//
//            // [2단계] 제미나이 분석기 가동
//            ProcessBuilder analysisPb = new ProcessBuilder(pythonExePath, analysisScript);
//            analysisPb.directory(new File(workingDirPath));
//            analysisPb.environment().put("PYTHONIOENCODING", "UTF-8");
//            analysisPb.inheritIO();
//            this.analysisProcess = analysisPb.start();
//            System.out.println("🤖 [시스템] 제미나이 분석기가 가동되어 백그라운드에서 요리를 시작합니다!");

            // [3단계] AI 벡터 서버 가동
            ProcessBuilder serverPb = new ProcessBuilder(pythonExePath, serverScript);
            serverPb.directory(new File(workingDirPath));
            serverPb.environment().put("PYTHONIOENCODING", "UTF-8");
            serverPb.inheritIO();
            this.serverProcess = serverPb.start();
            System.out.println("🚀 [시스템] 모든 파이썬 프로세스가 지독하게 압도적으로 가동되었습니다.");

        } catch (IOException e) {
            System.err.println("❌ [에러] 벡터 서버 실행 실패: " + e.getMessage());
        }
    }

    @PreDestroy
    public void stopPythonServer() {
        if (this.serverProcess != null && this.serverProcess.isAlive()) {
            this.serverProcess.destroy();
        }
//        if (this.fillerProcess != null && this.fillerProcess.isAlive()) {
//            this.fillerProcess.destroy();
//        }
//        if (this.analysisProcess != null && this.analysisProcess.isAlive()) {
//            this.analysisProcess.destroy();
//        }
        System.out.println("🛑 [시스템] 모든 파이썬 프로세스를 종료했습니다.");
    }
}