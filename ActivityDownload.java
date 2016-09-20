package com.example.webview;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.example.webview.Async_httpurl.downloadCompleteListener;
import com.example.webview.DownloadComplete.onComplteItemClickListener;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import kr.agworks.sedisk.common.util.Logger;

@SuppressLint("NewApi")
public class ActivityDownload extends Activity {

	ViewPager pager; // ViewPager 참조변수
	private final static String TAG = "DownloadActivity";
	private DownloadListAdapter downAdapter;
	private DownloadComplete completeAdapter;

	private String downloadFile = "Sedisk_DownloadFile";
	private File fileList;
	public static String root = null;
	private Button mBtnDownloading;
	private Button mBtnComplete;
	private File originFil;

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Button bt = (Button) findViewById(R.id.back);

		bt.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(ActivityDownload.this, ActivityMain.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				startActivity(intent);
			}
		});

		downAdapter = new DownloadListAdapter(getApplicationContext(), listner);

		Intent intent = getIntent();
		ArrayList<Item> info = intent.getParcelableArrayListExtra("List");

		if (info == null) {

		} else if (info != null) {
			for (Item item : info) {

				fileList = Environment.getExternalStorageDirectory();
				root = fileList.getAbsolutePath();
				Logger.d(TAG, "root : " + root);
				File file = new File(root + "/" + downloadFile);
				if (!file.exists()) {
					file.mkdirs();
				}
				item.downFile = new File(file + "/" + item.realName);
				Logger.d(TAG, "downFile path : " + item.downFile);
				downAdapter.addItem(item);
			}
		}
		pager = (ViewPager) findViewById(R.id.vp_main);
		CustomAdapter adapter = new CustomAdapter(getLayoutInflater());
		pager.setAdapter(adapter);

		/**
		 * 액션바 구현 부분
		 */

		// ViewPager에게 Page의 변경을 인지하는 Listener 세팅.
		// 마치 클릭상황을 인지하는 OnClickListener와 같은 역할..
		pager.setOnPageChangeListener(new OnPageChangeListener() {

			// 이전 Page의 80%가 이동했을때 다음 Page가 현재 Position으로 설정됨.
			// 파라미터 : 현재 변경된 Page의 위치
			@Override
			public void onPageSelected(final int position) {
				// TODO Auto-generated method stub
				changeMenuLayout(position);
			}

			@Override
			// 좌우 플리킹이 성공할때까지 계속 호출하며, 안착된다면 호출이 멈추게 된다.
			public void onPageScrolled(final int arg0, final float arg1, final int arg2) {
				// TODO Auto-generated method stub
			}

			@Override
			// onPageScrollStateChanged는 onpageScrolled보다 먼저 호출되면 후출이 시작되면 좌우
			// 플리킹을 시작점을 알려준다.
			public void onPageScrollStateChanged(final int arg0) {
				// TODO Auto-generated method stub
			}
		});
		setLayout();

	}

	private void changeMenuLayout(int position) {
		if (position == 0) {
			mBtnDownloading.setBackgroundResource(R.drawable.download_title_on);
			mBtnComplete.setBackgroundResource(R.drawable.download_title_off);
		} else if (position == 1) {
			mBtnDownloading.setBackgroundResource(R.drawable.download_title_off);
			mBtnComplete.setBackgroundResource(R.drawable.download_title_on);
		}
	}

	private void setLayout() {
		// TODO Auto-generated method stub
		mBtnDownloading = (Button) findViewById(R.id.btn_downloading);
		mBtnComplete = (Button) findViewById(R.id.btn_complete);

		mBtnDownloading.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (pager.getCurrentItem() != 0)
					pager.setCurrentItem(0);
			}
		});

		mBtnComplete.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (pager.getCurrentItem() != 1)
					pager.setCurrentItem(1);
			}
		});

	}

	public class CustomAdapter extends PagerAdapter {

		private LayoutInflater inflater;

		public CustomAdapter(LayoutInflater inflater) {
			// TODO Auto-generated constructor stub
			// 전달 받은 LayoutInflater를 멤버변수로 사용하기 위해서 전달 받는다.
			this.inflater = inflater;
		}

		// Tab에 따른 View를 보여줘야 하므로 Tab의 개수인 2을 리턴..
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return 2;
		}

		@Override
		// Count에서 얻어온 값을 position별로 pager에 등록할 item을 처리할 수 있도록 하는 메서드 입니다.
		public Object instantiateItem(ViewGroup container, int position) {
			// TODO Auto-generated method stub
			View view = null;// 현재 position에서 보여줘야할 View를 생성해서 리턴...
			Item downFile = null;
			if (position == 0) {
				// view = inflater.inflate(R.layout.item_file, null);
				view = inflater.inflate(R.layout.lvdownload, null);
				ListView list = (ListView) view.findViewById(R.id.lvdownload);
				list.setAdapter(downAdapter);

			} else if (position == 1) {
				String dirName = "트와이스";
				
				view = inflater.inflate(R.layout.lvcomplete, null);
				ListView lvComplte = (ListView) view.findViewById(R.id.lvcomplete);
				ArrayList<Item> items = new ArrayList<>();

				// 내부저장소 저장되는 path설정
				fileList = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
				File originFile = new File(fileList + "/" + dirName);
				Item item = new Item("url", "트와이스", originFile);

				items.add(item);

				if (originFile.exists()) {
					Logger.d(TAG, "originFile path : " + originFile.getPath());
				}

				completeAdapter = new DownloadComplete(getBaseContext(), items, new onComplteItemClickListener() {
					
					@Override
					public void permissionCheck() {
						if (Build.VERSION.SDK_INT == Build.VERSION_CODES.M) {

							/*
							 * 사용자 단말기의 권한 중 "전화걸기" 권한이 허용되어 있는지 체크한다. int를 쓴
							 * 이유? 안드로이드는 C기반이기 때문에, Boolean 이 잘 안쓰인다.
							 */
							int permissionResult = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);

							/* CALL_PHONE의 권한이 없을 때 */
							// 패키지는 안드로이드 어플리케이션의 아이디다.( 어플리케이션 구분자 )
							if (permissionResult == PackageManager.PERMISSION_DENIED) {

								/*
								 * 사용자가 CALL_PHONE 권한을 한번이라도 거부한 적이 있는 지 조사한다.
								 * 거부한 이력이 한번이라도 있다면, true를 리턴한다.
								 */
								if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

									AlertDialog.Builder dialog = new AlertDialog.Builder(ActivityDownload.this);
									dialog.setTitle("권한이 필요합니다.")
											.setMessage("이 기능을 사용하기 위해서는 단말기의 \"외장메모리\" 권한이 필요합니다. 계속하시겠습니까?")
											.setPositiveButton("네", new DialogInterface.OnClickListener() {
												@Override
												public void onClick(DialogInterface dialog, int which) {

													if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
														requestPermissions(
																new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE }, 1000);
													}

												}
											}).setNegativeButton("아니오", new DialogInterface.OnClickListener() {
												@Override
												public void onClick(DialogInterface dialog, int which) {
													Toast.makeText(ActivityDownload.this, "기능을 취소했습니다.",
															Toast.LENGTH_SHORT).show();
												}
											}).create().show();
								}

								// 최초로 권한을 요청할 때
								else {
									// WRITE_EXTERNAL_STORAGE 권한을 Android OS 에 요청한다.
									requestPermissions(new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE }, 1000);
								}

							}
							/* WRITE_EXTERNAL_STORAGE의 권한이 있을 때 */
							else {
								Toast.makeText(ActivityDownload.this, "외장메모리 권한 ON 상태입니다..",Toast.LENGTH_SHORT).show();
							}
						}
					}

					/**
					 * 사용자가 권한을 허용했는지 거부했는지 체크
					 * 
					 * @param requestCode1000번
					 * @param permissions 개발자가 요청한 권한들
					 * @param grantResults 권한에 대한 응답들 permissions와 grantResults는 인덱스 별로 매칭된다.
					 */
					@Override
					public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
							@NonNull int[] grantResults) {
						if (requestCode == 1000) {

							/*
							 * 요청한 권한을 사용자가 "허용"했다면 인텐트를 띄워라 내가 요청한 게 하나밖에 없기
							 * 때문에. 원래 같으면 for문을 돈다.
							 */
							if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
								Toast.makeText(ActivityDownload.this, "권한 요청이 이루어 졌습니다.", Toast.LENGTH_SHORT).show();
								}
							} else {
								Toast.makeText(ActivityDownload.this, "권한 요청을 거부했습니다.", Toast.LENGTH_SHORT).show();
							}

						
					}

					@Override
					public void sdCard(Item item) {
						// TODO Auto-generated method stub
						new Async_SdCard_tst(ActivityDownload.this, item).execute();
						Logger.d(TAG, "Item : " + item);

					}

					@Override
					public void isStorage() {
						// TODO Auto-generated method stub
						
					}
				});
				lvComplte.setAdapter(completeAdapter);
			}

			// ViewPager에 위에서 만들어 낸 View 추가
			if (view != null)
				container.addView(view);
			// 세팅된 View를 리턴
			return view;
		}

		public List<File> getDirFileList(String dirOriginFile) {
			{
				// 디렉토리 파일 리스트
				List<File> dirFileList = null;

				// 파일 목록을 요청한 디렉토리를 가지고 파일 객체를 생성함
				File dir = new File(dirOriginFile);

				// 디렉토리가 존재한다면
				if (dir.exists()) {
					// 파일 목록을 구함
					File[] files = dir.listFiles();

					// 파일 배열을 파일 리스트로 변화함
					dirFileList = Arrays.asList(files);
				}

				return dirFileList;
			}
		}

		// instantiateItem() 메소드에서 리턴된 Ojbect가 View가 맞는지 확인하는 메소드
		@Override
		public boolean isViewFromObject(View v, Object obj) {
			// TODO Auto-generated method stub
			return v == obj;
		}
	} // CustomAdapter 종료

	private downloadCompleteListener listner = new downloadCompleteListener() {

		@Override
		public void onComplete(Item item) {
			completeAdapter.addItem(item);
			downAdapter.removeItem(item);
		}
	};

}