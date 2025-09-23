INSERT INTO item(id, title, image_path, description, price) VALUES
  (
      1,
      'Бейсболка BeOneDesire',
      'https://ir.ozone.ru/s3/multimedia-0/wc1000/6178456572.jpg',
      'Стильная бейсболка Black Rebel представлена в 5-ти цветах. Подойдет для повседневной носки, достаточно удобная, размер регулируется.',
      900
  ),
  (
      2,
      'Футболка Donald Pump',
      'https://ae04.alicdn.com/kf/S8feb68ce4c874cbdb09c03fad76604b2K.png',
      'Make America Strong Again!',
      1700
  ),
  (
      3,
      'Джинсы',
      'https://ir.ozone.ru/s3/multimedia-1-3/wc1000/7548650535.jpg',
      'Наши джинсы подходят для любого случая: от повседневной носки и работы до вечеринок и свиданий. Они идеально сочетаются с активной жизнью, прогулками, танцами и путешествиями. Вы сможете создавать стильные образы для социальных событий, общения с друзьями, творческих проектов и модных фотосессий.',
      5200
  ),
  (
      4,
      'Лоферы Loro Piana',
      'https://ir.ozone.ru/s3/multimedia-1-7/wc1000/7748440351.jpg',
      'Лоферы Loro Piana — это воплощение роскоши и изысканности!',
      15600
  )
ON CONFLICT DO NOTHING;