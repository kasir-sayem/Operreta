# Hungarian Operetta Database Application

## Project Overview
This JavaFX desktop application serves as a comprehensive management system for Hungarian operetta data. The application provides CRUD functionality, data visualization, parallel programming demonstration, and integration with external web services.

## Database Structure
The application uses an SQLite database with the following structure:

### Tables
1. **works** - Main operetta works
   - `id`: Unique identifier
   - `title`: Hungarian title
   - `original`: Original title (if different)
   - `theatre`: Theatre where premiered
   - `pyear`: Premiere year
   - `acts`: Number of acts
   - `scenes`: Number of scenes

2. **creators** - Composers, librettists, and translators
   - `id`: Unique identifier
   - `cname`: Creator's name

3. **connections** - Links works with their creators
   - `id`: Unique identifier
   - `workid`: Reference to works
   - `ctype`: Type of connection (music, libretto, translation)
   - `creatorid`: Reference to creators

## Features

### Database Menu
- **Read**: View operetta data from all tables with filtering options
- **Read2**: Advanced filtering with various input types
- **Write**: Add new operettas or creators to the database
- **Change**: Modify existing records using dropdown selection
- **Delete**: Remove records from the database

### SOAP Client Menu
- **Download**: Retrieve currency exchange rate data from Hungarian National Bank (MNB)
- **Download2**: Customized data selection for download
- **Graph**: Visualize exchange rate data on charts

### Parallel Programming Menu
- Demonstration of multithreading with visual elements updating at different intervals

### Forex Menu (Oanda API Integration)
- **Account Information**: View account details
- **Current Prices**: Check live currency pair prices
- **Historical Prices**: View and graph historical price data
- **Position Opening**: Open trading positions
- **Position Closing**: Close existing positions
- **Opened Positions**: Monitor active trades

## Technical Requirements

### Prerequisites
- Java 11 or higher
- JavaFX runtime
- Internet connection for web services

### Installation
1. Download the `DOWNLOAD.ZIP` file
2. Extract the contents
3. Ensure the database file `data.db` is located in the `c:\data` folder
4. Run the JAR file with: `java -jar HungarianOperettaApp.jar`

### Development Setup
1. Clone the repository
2. Open the project in your preferred Java IDE
3. Ensure JavaFX SDK is properly configured
4. Build and run the application

## Project Structure
```
src/
├── main/
│   ├── java/
│   │   ├── controller/
│   │   ├── model/
│   │   ├── service/
│   │   ├── util/
│   │   └── view/
│   └── resources/
│       ├── css/
│       ├── fxml/
│       └── images/
└── test/
    └── java/
```

## Contributing
This project follows the standard GitHub workflow:
1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Submit a pull request

## License
[MIT License](LICENSE)