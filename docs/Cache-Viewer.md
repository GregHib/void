The cache viewer is a tool to quickly and easily search through game [definition](definitions) data, typically when writing content.

If you're playing in-game it's better to use the [/find command](Commands#unlock).

## Setup
A prebuilt jar can be found here: [download link](https://mega.nz/folder/FFcDgKAB#FqARRP5MSx69S15--7MZNA).

Alternatively you can run the definition-browser using the following gradle command:

```bash
./gradlew tools:app:run
```

From there you'll be presented with a screen where you should select to your `/void/data/` directory.

<img width="986" height="593" alt="image" src="https://github.com/user-attachments/assets/300feff9-97ab-499b-8d0f-aaf8ecfae47b" />

> [!WARNING]
> `void/data/cache/` will work but won't load data from [config files](config-files)

## Layout

Once the cache is loaded (this can take a minute) you'll see the main interface.

<img width="986" height="593" alt="image" src="https://github.com/user-attachments/assets/1065c138-6e62-4928-96df-1ff5daf49eaf" />

On the top row you'll find a Global search "All", a list of different types of definitions, and a reload and change directory button.


### Search All
In the All tab you'll find a search bar where you can enter any word, name or id and it'll search within all definitions.

<img width="986" height="593" alt="image" src="https://github.com/user-attachments/assets/a54c58d2-ebd4-47dd-9dc8-619c3d54e0a7" />

Clicking on a result will show [details](#details), or redirect you to the result in the main tab for that definition type.

### Details

The details panel displays lots of information about the selected definition, everything from options, to parameters loading from [config-files](config-files)

<img width="296" height="521" alt="image" src="https://github.com/user-attachments/assets/8501727d-9d55-498a-ae1f-f77d6b763dcb" />
<img width="284" height="466" alt="image" src="https://github.com/user-attachments/assets/2ff62957-81b3-482b-81ab-53b91ee0f83f" />
<img width="282" height="258" alt="image" src="https://github.com/user-attachments/assets/00dacfb1-d654-46cc-9566-364ecaa25aac" />


### Filter

In the main definition tabs you can view all the definition's for a given type and filter by any value

<img width="986" height="634" alt="image" src="https://github.com/user-attachments/assets/f75d30df-b55c-47d3-93d0-afddc6919235" />

You can select whichever columns you want to display, preview the [details](#details)
<img width="986" height="634" alt="image" src="https://github.com/user-attachments/assets/2d046580-1423-4752-a137-be3d2678bdae" />

Multi-select, and copy to clipboard with ctrl + c or right click

<img width="986" height="634" alt="image" src="https://github.com/user-attachments/assets/2836c434-b43b-4c73-9e41-007ec59c1eb0" />




