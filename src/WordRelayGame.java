import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class WordRelayGame {

	public static void main(String[] args) {

		int checker = 0, playing = 0;
		Scanner input = new Scanner(System.in);
		NewGame game = new NewGame();

		try {
			game.gameSet();
		} catch (IOException e1) {
			e1.printStackTrace();
			System.out.println("사전 읽기 과정에서 오류가 발생하였습니다.");
			playing = 1;
		}

		while (playing == 0) {
			game.gameStart();
			checker = 0;
			while (checker == 0) {

				String playerWord = input.nextLine().trim();

				int cacheCheck = game.readWord(playerWord);
				int cacheCheck2 = 0;
				if (cacheCheck == 0)
					checker = 2;
				if (cacheCheck == 1)
					cacheCheck2 = game.speakWord(playerWord);

				if (cacheCheck2 == 1)
					checker = 1;

			}

			if (checker == 1)
				game.win();
			if (checker == 2)
				game.lose();

			String playerWord = input.nextLine();
			if (playerWord.equals("1"))
				playing = 0;
			else
				playing = 1;

		}

		try {
			game.gameEnd();
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("사전 저장 과정에서 오류가 발생하였습니다.");
		}
	}
}

class NewGame {

	List<String> dic = new ArrayList<String>();
	String[] cache;
	String lastSpeakWordEnd;
	int[] cache2 = new int[10000];
	int dicWordCount = 0;

	public void gameSet() throws IOException {
		
		FileInputStream fis = new FileInputStream(new File("data/dic.txt"));
		
		InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
		
		BufferedReader br = new BufferedReader(isr);
		
		//BufferedReader br = new BufferedReader(new FileReader("data/dic.txt"));

		while (true) {
			String line = br.readLine();
			if (line == null) break;
			dic.add(line);
			// System.out.println(cache[dicWordCount]);
			dicWordCount++;
			
		}
		dicWordCount--;
		br.close();

	}

	public void gameStart() {
		System.out.println("끝말잇기!! v1.0.0 (BUILD 14)");
		System.out.println("단어를 적고 엔터를 쳐 주세요. 네이버 사전을 기준으로 단어를 인정합니다.");
		System.out.println("(명사중 인명, 지명, 북한어를 제외합니다.)");
		System.out.println("현재 " + dicWordCount + "개의 단어가 데이터베이스에 있습니다.");
		System.out.println("부가 설명이 필요하면 '?' 를 쳐 주세요.");
		for (int i = 0; i < dicWordCount; i++) {
			cache2[i] = -1;
		}
		Collections.shuffle(dic);
		cache = (String[]) dic.toArray(new String[dic.size()]);
		lastSpeakWordEnd = null;
	}

	public void gameHelp() {
		System.out.println("도움말은 제공되지 않습니다.");
	}

	public void naverNextDicWordSearch(String endWord){
		
	}
	
	public int naverDicWordSearch(String playerWord) {

		String dicUrl, playerWordUTF8;
		Document document;
		Elements dicData, dataType, dataRealType, dataRealType2;
		try {
			playerWordUTF8 = URLEncoder.encode(playerWord, "UTF-8");
			dicUrl = "http://m.krdic.naver.com/search/entry/1/" + playerWordUTF8;
			document = Jsoup.connect(dicUrl).get();
			for (int i = 0; i < 3; i++) {
				dicData = document.select("div.se ul#viewMoreList.lst li:eq(" + i + ") div.dt a.ft strong.str2");
				dataType = document.select("div.se ul#viewMoreList.lst li:eq(" + i + ") p.sy");//명사, 동사등 분류
				//인명, 지명 등..
				dataRealType = document.select("div.se ul#viewMoreList.lst li:eq(" + i + ") p.sy span.foa");
				//북한
				dataRealType2 = document.select("div.se ul#viewMoreList.lst li:eq(" + i + ") p.sy span.sya");
				String[] cacheArr = new String[4];
				cacheArr[0] = dicData.text();
				cacheArr[1] = dataType.text();
				cacheArr[2] = dataRealType.text();
				cacheArr[3] = dataRealType2.text();

				if (cacheArr[0] != null && cacheArr[1].length() > 3) {
					cacheArr[1] = cacheArr[1].substring(1, 3);
					System.out.println(cacheArr[0] + ":" + cacheArr[1] + ":" + cacheArr[2] + ":" + cacheArr[3]);
					if (cacheArr[0].equals(playerWord) && cacheArr[1].equals("명사")
							&& (cacheArr[2] == null || !(cacheArr[2].equals("<인명>") || cacheArr[2].equals("<지명>"))) && (cacheArr[3] == null || !cacheArr[3].equals("[북한어]"))) {
						System.out.println("적정 단어입니다.");
						dic.add(cacheArr[0]);
						
						if (lastSpeakWordEnd == null) return 1;
						if (lastSpeakWordEnd.equals(playerWord.substring(0, 1))) return 1;
						return 0;
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return 0;

	}

	public int readWord(String playerWord) {
		//System.out.println(lastSpeakWordEnd + dicWordCount);
		int playerWordExist = 0;
		if(playerWord.length() == 1) return 0;
		if (lastSpeakWordEnd == null) {

			for (int i = 0; i < dicWordCount; i++) {
				if (playerWord.equals(cache[i])) {
					playerWordExist = 1;
					if (cache2[i] == -1) {
						cache2[i] = 1;
						return 1;// player win
					}
				}
			}
			
			if (playerWordExist == 0){
				System.out.println("checking naver dictonary");
				return naverDicWordSearch(playerWord);
			}

		} else {
			for (int i = 0; i < dicWordCount; i++) {
				if (playerWord.equals(cache[i])) {
					playerWordExist = 1;
					if (cache2[i] == -1 && lastSpeakWordEnd.equals(playerWord.substring(0, 1))) {
						cache2[i] = 1;
						return 1;// player win
					}
				}
			}
			if (playerWordExist == 0){
				System.out.println("checking naver dictonary");
				return naverDicWordSearch(playerWord);
			}
		}

		return 0;
	}

	public int speakWord(String playerWord) {
		String playerWordEnd = playerWord.substring(playerWord.length() - 1, playerWord.length());
		for (int i = 0; i < dicWordCount; i++) {
			if (cache2[i] == -1 && playerWordEnd.equals(cache[i].substring(0, 1))) {
				System.out.println("AI : " + cache[i]);
				lastSpeakWordEnd = cache[i].substring(cache[i].length() - 1, cache[i].length());
				cache2[i] = 1;
				return 0;
			}
		}

		return 1;// player win
	}

	public void win() {
		System.out.println("승리하였습니다.");
		System.out.println("다시 플레이하시겠습니까? (yes : 1  /  no : 2)");
	}

	public void lose() {
		System.out.println("패배하였습니다.");
		System.out.println("다시 플레이하시겠습니까? (yes : 1  /  no : 2)");
	}

	public void gameEnd() throws IOException {
		System.out.println("게임을 플레이하여 주셔서 감사합니다.");
		PrintWriter pw = new PrintWriter(System.getProperty("user.home") + "/Documents/dic.txt");
		cache = (String[]) dic.toArray(new String[dic.size()]);
		for (int i = 0; i < cache.length-1; i++) {
			pw.println(cache[i]);
		}
		pw.printf(cache[cache.length-1]);
		pw.close();

	}

}
